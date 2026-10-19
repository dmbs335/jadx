.class public Ltrycatch/TestTernaryDefaultAfterEmptyBranch;
.super Ljava/lang/Object;

.method public static update(Ljava/util/concurrent/atomic/AtomicReference;Ljava/lang/Object;Ljava/lang/Throwable;)V
    .registers 5

    if-nez p0, :check_exception
    return-void

    :check_exception
    if-nez p2, :unwrap
    invoke-virtual {p0, p1}, Ljava/util/concurrent/atomic/AtomicReference;->set(Ljava/lang/Object;)V
    goto :end

    :unwrap
    instance-of v0, p2, Ljava/util/concurrent/CompletionException;
    if-eqz v0, :not_completion
    move-object v0, p2
    check-cast v0, Ljava/util/concurrent/CompletionException;
    goto :check_cause

    :not_completion
    const/4 v0, 0x0

    :check_cause
    if-eqz v0, :fallback
    invoke-virtual {v0}, Ljava/util/concurrent/CompletionException;->getCause()Ljava/lang/Throwable;
    move-result-object v0
    if-nez v0, :emit

    :fallback
    move-object v0, p2

    :emit
    invoke-virtual {p0, v0}, Ljava/util/concurrent/atomic/AtomicReference;->set(Ljava/lang/Object;)V

    :end
    return-void
.end method
