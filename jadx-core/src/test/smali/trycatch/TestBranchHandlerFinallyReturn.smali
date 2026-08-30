.class public Ltrycatch/TestBranchHandlerFinallyReturn;
.super Ljava/lang/Object;

.field public callback:Ljava/util/function/Consumer;
.field public input:Ljava/util/concurrent/Future;
.field public output:Ljava/util/concurrent/Future;
.field public result:Ljava/util/concurrent/Future;

.method private static cleanup()V
    .locals 0

    return-void
.end method

.method public final run()V
    .locals 4

    const/4 v0, 0x0

    :try_start_input
    iget-object v1, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->input:Ljava/util/concurrent/Future;
    invoke-interface {v1}, Ljava/util/concurrent/Future;->get()Ljava/lang/Object;
    move-result-object v1
    :try_end_input
    .catch Ljava/util/concurrent/CancellationException; {:try_start_input .. :try_end_input} :catch_cancel
    .catch Ljava/util/concurrent/ExecutionException; {:try_start_input .. :try_end_input} :catch_execution
    .catch Ljava/lang/reflect/UndeclaredThrowableException; {:try_start_input .. :try_end_input} :catch_undeclared
    .catch Ljava/lang/Exception; {:try_start_input .. :try_end_input} :catch_exception
    .catch Ljava/lang/Error; {:try_start_input .. :try_end_input} :catch_error
    .catchall {:try_start_input .. :try_end_input} :catch_all

    :try_start_body
    iget-object v1, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->output:Ljava/util/concurrent/Future;
    iput-object v1, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->result:Ljava/util/concurrent/Future;

    invoke-interface {v1}, Ljava/util/concurrent/Future;->isCancelled()Z
    move-result v2
    if-eqz v2, :body_notify

    const/4 v2, 0x0
    invoke-interface {v1, v2}, Ljava/util/concurrent/Future;->cancel(Z)Z
    iput-object v0, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->result:Ljava/util/concurrent/Future;
    goto :normal_cleanup

    :body_notify
    iget-object v2, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->callback:Ljava/util/function/Consumer;
    if-eqz v2, :normal_cleanup
    invoke-interface {v2, v1}, Ljava/util/function/Consumer;->accept(Ljava/lang/Object;)V
    :try_end_body
    .catch Ljava/lang/reflect/UndeclaredThrowableException; {:try_start_body .. :try_end_body} :catch_undeclared
    .catch Ljava/lang/Exception; {:try_start_body .. :try_end_body} :catch_exception
    .catch Ljava/lang/Error; {:try_start_body .. :try_end_body} :catch_error
    .catchall {:try_start_body .. :try_end_body} :catch_all

    :normal_cleanup
    iput-object v0, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->input:Ljava/util/concurrent/Future;
    iput-object v0, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->output:Ljava/util/concurrent/Future;
    invoke-static {}, Ltrycatch/TestBranchHandlerFinallyReturn;->cleanup()V
    return-void

    :catch_all
    move-exception v1
    goto :finally_throw

    :catch_error
    move-exception v1
    goto :handle_error

    :catch_exception
    move-exception v1
    goto :handle_exception

    :catch_undeclared
    move-exception v1
    goto :handle_undeclared

    :catch_execution
    move-exception v1
    invoke-virtual {v1}, Ljava/lang/Throwable;->getCause()Ljava/lang/Throwable;
    move-result-object v1
    iget-object v2, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->callback:Ljava/util/function/Consumer;
    if-eqz v2, :normal_cleanup
    invoke-interface {v2, v1}, Ljava/util/function/Consumer;->accept(Ljava/lang/Object;)V
    goto :normal_cleanup

    :catch_cancel
    const/4 v1, 0x0
    :try_start_cancel
    iget-object v2, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->output:Ljava/util/concurrent/Future;
    invoke-interface {v2, v1}, Ljava/util/concurrent/Future;->cancel(Z)Z
    :try_end_cancel
    .catch Ljava/lang/reflect/UndeclaredThrowableException; {:try_start_cancel .. :try_end_cancel} :catch_undeclared
    .catch Ljava/lang/Exception; {:try_start_cancel .. :try_end_cancel} :catch_exception
    .catch Ljava/lang/Error; {:try_start_cancel .. :try_end_cancel} :catch_error
    .catchall {:try_start_cancel .. :try_end_cancel} :catch_all
    goto :normal_cleanup

    :handle_error
    :try_start_error_handler
    iget-object v2, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->callback:Ljava/util/function/Consumer;
    if-eqz v2, :handler_cleanup
    invoke-interface {v2, v1}, Ljava/util/function/Consumer;->accept(Ljava/lang/Object;)V
    :try_end_error_handler
    .catchall {:try_start_error_handler .. :try_end_error_handler} :catch_all
    goto :handler_cleanup

    :handle_exception
    :try_start_other_handlers
    iget-object v2, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->callback:Ljava/util/function/Consumer;
    if-eqz v2, :handler_cleanup
    invoke-interface {v2, v1}, Ljava/util/function/Consumer;->accept(Ljava/lang/Object;)V
    goto :handler_cleanup

    :handle_undeclared
    invoke-virtual {v1}, Ljava/lang/reflect/UndeclaredThrowableException;->getCause()Ljava/lang/Throwable;
    move-result-object v1
    iget-object v2, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->callback:Ljava/util/function/Consumer;
    if-eqz v2, :handler_cleanup
    invoke-interface {v2, v1}, Ljava/util/function/Consumer;->accept(Ljava/lang/Object;)V
    :try_end_other_handlers
    .catchall {:try_start_other_handlers .. :try_end_other_handlers} :catch_all

    :handler_cleanup
    iput-object v0, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->input:Ljava/util/concurrent/Future;
    iput-object v0, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->output:Ljava/util/concurrent/Future;
    invoke-static {}, Ltrycatch/TestBranchHandlerFinallyReturn;->cleanup()V
    goto :handler_return

    :handler_return
    return-void

    :finally_throw
    iput-object v0, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->input:Ljava/util/concurrent/Future;
    iput-object v0, p0, Ltrycatch/TestBranchHandlerFinallyReturn;->output:Ljava/util/concurrent/Future;
    invoke-static {}, Ltrycatch/TestBranchHandlerFinallyReturn;->cleanup()V
    throw v1
.end method
