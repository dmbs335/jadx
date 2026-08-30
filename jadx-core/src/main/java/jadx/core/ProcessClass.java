package jadx.core;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.DecompilationMode;
import jadx.api.ICodeInfo;
import jadx.api.JadxArgs;
import jadx.api.impl.SimpleCodeInfo;
import jadx.core.codegen.CodeGen;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.DecompileModeOverrideAttr;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.LoadStage;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.dex.visitors.DepthTraversal;
import jadx.core.dex.visitors.IDexTreeVisitor;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.core.utils.exceptions.JadxTaskCancelledException;

import static jadx.core.dex.nodes.ProcessState.GENERATED_AND_UNLOADED;
import static jadx.core.dex.nodes.ProcessState.LOADED;
import static jadx.core.dex.nodes.ProcessState.NOT_LOADED;
import static jadx.core.dex.nodes.ProcessState.PROCESS_COMPLETE;
import static jadx.core.dex.nodes.ProcessState.PROCESS_STARTED;

public class ProcessClass {
	private static final Logger LOG = LoggerFactory.getLogger(ProcessClass.class);

	private static final ICodeInfo NOT_GENERATED = new SimpleCodeInfo("");

	private final List<IDexTreeVisitor> passes;

	public ProcessClass(List<IDexTreeVisitor> passesList) {
		this.passes = passesList;
	}

	@Nullable
	private ICodeInfo process(ClassNode cls, boolean codegen) {
		if (!codegen && cls.getState() == PROCESS_COMPLETE) {
			// nothing to do
			return null;
		}
		Utils.checkThreadInterrupt();
		ReentrantLock decompileLock = cls.getDecompileLock();
		decompileLock.lock();
		try {
			try {
				prepareForProcessing(cls);
				if (cls.getState() == GENERATED_AND_UNLOADED) {
					// force loading code again
					cls.setState(NOT_LOADED);
				}
				if (codegen) {
					cls.setLoadStage(LoadStage.CODEGEN_STAGE);
					if (cls.contains(AFlag.RELOAD_AT_CODEGEN_STAGE)) {
						cls.remove(AFlag.RELOAD_AT_CODEGEN_STAGE);
						cls.unload();
					}
				} else {
					cls.setLoadStage(LoadStage.PROCESS_STAGE);
				}
				if (cls.getState() == NOT_LOADED) {
					cls.load();
				}
				if (cls.getState() == LOADED) {
					cls.setState(PROCESS_STARTED);
					for (IDexTreeVisitor visitor : passes) {
						Utils.checkThreadInterrupt();
						DepthTraversal.visit(visitor, cls);
					}
					cls.setState(PROCESS_COMPLETE);
				}
				if (codegen) {
					Utils.checkThreadInterrupt();
					ICodeInfo code = CodeGen.generate(cls);
					if (!cls.contains(AFlag.DONT_UNLOAD_CLASS)) {
						cls.unload();
						cls.setState(GENERATED_AND_UNLOADED);
					}
					return code;
				}
				return null;
			} catch (JadxTaskCancelledException e) {
				recoverAfterCancellation(cls);
				throw e;
			} catch (StackOverflowError | Exception e) {
				if (codegen) {
					throw e;
				}
				cls.addError("Class process error: " + e.getClass().getSimpleName(), e);
				return null;
			}
		} finally {
			decompileLock.unlock();
		}
	}

	private static void recoverAfterCancellation(ClassNode cls) {
		boolean restoreInterrupt = Thread.interrupted();
		try {
			cls.unloadFromCache();
			cls.deepUnload();
		} finally {
			if (restoreInterrupt) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Restore class data and pre-decompile attributes before reading its dependency lists.
	 * Must be called with the class lock held or from a path that does not yet expose the
	 * reloaded class to another processing thread.
	 */
	private static void prepareForProcessing(ClassNode cls) {
		if (cls.contains(AFlag.CLASS_DEEP_RELOAD)) {
			cls.remove(AFlag.CLASS_DEEP_RELOAD);
			cls.deepUnload();
			cls.add(AFlag.CLASS_UNLOADED);
		}
		if (cls.contains(AFlag.CLASS_UNLOADED)) {
			cls.root().runPreDecompileStageForClass(cls);
			cls.remove(AFlag.CLASS_UNLOADED);
		}
	}

	@NotNull
	public ICodeInfo generateCode(ClassNode cls) {
		ClassNode topParentClass = cls.getTopParentClass();
		if (topParentClass != cls) {
			return generateCode(topParentClass);
		}
		try {
			// Deep reload rebuilds dependency and codegen-dependency attributes. Do it
			// before traversing these lists, otherwise this generation uses stale data.
			ReentrantLock decompileLock = cls.getDecompileLock();
			decompileLock.lock();
			try {
				prepareForProcessing(cls);
			} finally {
				decompileLock.unlock();
			}
			if (cls.contains(AFlag.DONT_GENERATE)) {
				process(cls, false);
				return NOT_GENERATED;
			}
			List<ClassNode> codegenDeps = cls.getCodegenDeps();
			List<ClassNode> deferredDeps = null;
			for (ClassNode depCls : cls.getDependencies()) {
				if (depCls.getState() == GENERATED_AND_UNLOADED
						&& depCls.contains(AFlag.CLASS_DEEP_RELOAD)
						&& codegenDeps.contains(depCls)) {
					// Keep persistent information from the previous generation available
					// while processing this class. The dependency will be deeply reloaded
					// in its intended codegen-dependency slot below.
					continue;
				}
				if (!tryProcessDependency(depCls)) {
					if (deferredDeps == null) {
						deferredDeps = new ArrayList<>();
					}
					deferredDeps.add(depCls);
				}
			}
			if (deferredDeps != null) {
				for (ClassNode deferredDep : deferredDeps) {
					process(deferredDep, false);
				}
			}
			if (!codegenDeps.isEmpty()) {
				process(cls, false);
				for (ClassNode codegenDep : codegenDeps) {
					process(codegenDep, false);
				}
			}
			ICodeInfo code = process(cls, true);
			if (code == null) {
				throw new JadxRuntimeException("Codegen failed");
			}
			return code;
		} catch (JadxTaskCancelledException e) {
			throw e;
		} catch (StackOverflowError | Exception e) {
			throw new JadxRuntimeException("Failed to generate code for class: " + cls.getFullName(), e);
		}
	}

	private boolean tryProcessDependency(ClassNode cls) {
		if (cls.getState() == PROCESS_COMPLETE) {
			return true;
		}
		ReentrantLock lock = cls.getDecompileLock();
		if (!lock.tryLock()) {
			return false;
		}
		try {
			process(cls, false);
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Load and process class without its deps
	 */
	public void forceProcess(ClassNode cls) {
		ClassNode topParentClass = cls.getTopParentClass();
		if (topParentClass != cls) {
			forceProcess(topParentClass);
			return;
		}
		try {
			process(cls, false);
		} catch (JadxTaskCancelledException e) {
			throw e;
		} catch (StackOverflowError | Exception e) {
			throw new JadxRuntimeException("Failed to process class: " + cls.getFullName(), e);
		}
	}

	/**
	 * Generate code for class without processing its deps
	 */
	public @Nullable ICodeInfo forceGenerateCode(ClassNode cls) {
		try {
			return process(cls, true);
		} catch (JadxTaskCancelledException e) {
			throw e;
		} catch (StackOverflowError | Exception e) {
			throw new JadxRuntimeException("Failed to generate code for class: " + cls.getFullName(), e);
		}
	}

	private final Map<DecompilationMode, ProcessClass> modesMap = new EnumMap<>(DecompilationMode.class);

	public @Nullable ICodeInfo forceGenerateCodeForMode(ClassNode cls, DecompilationMode mode) {
		synchronized (modesMap) {
			ProcessClass prCls = modesMap.computeIfAbsent(mode, m -> {
				RootNode root = cls.root();
				ProcessClass newPrCls = new ProcessClass(getPassesForMode(root.getArgs(), m));
				newPrCls.initPasses(root);
				return newPrCls;
			});
			try {
				cls.addAttr(new DecompileModeOverrideAttr(mode));
				return prCls.forceGenerateCode(cls);
			} finally {
				cls.remove(AType.DECOMPILE_MODE_OVERRIDE);
			}
		}
	}

	private static List<IDexTreeVisitor> getPassesForMode(JadxArgs baseArgs, DecompilationMode mode) {
		switch (mode) {
			case FALLBACK:
				return Jadx.getFallbackPassesList();

			case SIMPLE:
				// copy properties into new args
				// keep in sync with properties usage in Jadx.getSimpleModePasses method
				JadxArgs args = new JadxArgs();
				args.setDebugInfo(baseArgs.isDebugInfo());
				args.setCommentsLevel(baseArgs.getCommentsLevel());
				return Jadx.getSimpleModePasses(args);

			default:
				throw new JadxRuntimeException("Unexpected decompilation mode: " + mode);
		}
	}

	public void initPasses(RootNode root) {
		for (IDexTreeVisitor pass : passes) {
			try {
				pass.init(root);
			} catch (JadxTaskCancelledException e) {
				throw e;
			} catch (Exception e) {
				LOG.error("Visitor init failed: {}", pass.getClass().getSimpleName(), e);
			}
		}
	}

	public boolean processMethodUntilVisitor(MethodNode mth, String visitorName, boolean includeVisitor) {
		IDexTreeVisitor foundPass = null;
		IDexTreeVisitor prevPass = null;
		for (IDexTreeVisitor pass : passes) {
			if (pass.getName().equals(visitorName)) {
				if (includeVisitor) {
					foundPass = pass;
				} else {
					foundPass = prevPass;
				}
				break;
			}
			prevPass = pass;
		}
		if (foundPass == null) {
			return false;
		}
		return processMethodToVisitor(mth, foundPass);
	}

	public boolean processMethodToVisitor(MethodNode mth, IDexTreeVisitor lastPassToProcess) {
		ClassNode topCls = mth.getTopParentClass();
		ReentrantLock decompileLock = topCls.getDecompileLock();
		decompileLock.lock();
		try {
			try {
				mth.unload();
				mth.load();
				for (IDexTreeVisitor pass : passes) {
					Utils.checkThreadInterrupt();
					DepthTraversal.visit(pass, mth);
					if (pass == lastPassToProcess) {
						return true;
					}
				}
			} catch (JadxTaskCancelledException e) {
				recoverAfterCancellation(topCls);
				throw e;
			} catch (Exception e) {
				throw new JadxRuntimeException("Failed to process method to visitor: " + lastPassToProcess, e);
			}
			return false;
		} finally {
			decompileLock.unlock();
		}
	}

	// TODO: make passes list private and not visible
	public List<IDexTreeVisitor> getPasses() {
		return passes;
	}
}
