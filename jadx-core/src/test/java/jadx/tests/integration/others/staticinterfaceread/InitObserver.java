package jadx.tests.integration.others.staticinterfaceread;

public interface InitObserver {
	int OBSERVED = StaticInterfaceChild.readInitial();

	default void marker() {
	}
}
