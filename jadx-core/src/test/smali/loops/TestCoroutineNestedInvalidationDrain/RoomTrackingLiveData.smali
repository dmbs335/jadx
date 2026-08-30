.class public abstract Landroidx/room/RoomTrackingLiveData;
.super Landroidx/lifecycle/LiveData;
.source "RoomTrackingLiveData.android.kt"

.field public final k:Landroidx/room/RoomDatabase;
.field public final n:Landroidx/room/InvalidationTracker$Observer;
.field public final o:Ljava/util/concurrent/atomic/AtomicBoolean;
.field public final p:Ljava/util/concurrent/atomic/AtomicBoolean;
.field public final q:Ljava/util/concurrent/atomic/AtomicBoolean;

.method public abstract compute(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method public final k(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 8

    instance-of v0, p1, Landroidx/room/RoomTrackingLiveData$refresh$1;
    if-eqz v0, :new_continuation

    move-object v0, p1
    check-cast v0, Landroidx/room/RoomTrackingLiveData$refresh$1;
    iget v1, v0, Landroidx/room/RoomTrackingLiveData$refresh$1;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation

    sub-int/2addr v1, v2
    iput v1, v0, Landroidx/room/RoomTrackingLiveData$refresh$1;->label:I
    goto :continuation_ready

    :new_continuation
    new-instance v0, Landroidx/room/RoomTrackingLiveData$refresh$1;
    invoke-direct {v0, p0, p1}, Landroidx/room/RoomTrackingLiveData$refresh$1;-><init>(Landroidx/room/RoomTrackingLiveData;Lkotlin/coroutines/Continuation;)V

    :continuation_ready
    iget-object p1, v0, Landroidx/room/RoomTrackingLiveData$refresh$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Landroidx/room/RoomTrackingLiveData$refresh$1;->label:I
    const/4 v3, 0x0
    const/4 v4, 0x1
    if-eqz v2, :initial_state
    if-ne v2, v4, :invalid_state

    iget v2, v0, Landroidx/room/RoomTrackingLiveData$refresh$1;->I$0:I
    :try_start_resume
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume
    .catch Ljava/lang/Exception; {:try_start_resume .. :try_end_resume} :catch_compute
    .catchall {:try_start_resume .. :try_end_resume} :catchall_compute
    goto :drain_header

    :catchall_compute
    move-exception p1
    goto :release_and_throw

    :catch_compute
    move-exception p1
    goto :wrap_exception

    :invalid_state
    new-instance p1, Ljava/lang/IllegalStateException;
    const v0, 0x624cfed3
    invoke-static {v0}, Lfixtures/obfuscation/StringDecoder;->decode(I)Ljava/lang/String;
    move-result-object v0
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1

    :initial_state
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object p1, p0, Landroidx/room/RoomTrackingLiveData;->q:Ljava/util/concurrent/atomic/AtomicBoolean;
    invoke-virtual {p1, v3, v4}, Ljava/util/concurrent/atomic/AtomicBoolean;->compareAndSet(ZZ)Z
    move-result p1
    if-eqz p1, :outer_header

    iget-object p1, p0, Landroidx/room/RoomTrackingLiveData;->k:Landroidx/room/RoomDatabase;
    invoke-virtual {p1}, Landroidx/room/RoomDatabase;->getInvalidationTracker()Landroidx/room/InvalidationTracker;
    move-result-object p1
    iget-object v2, p0, Landroidx/room/RoomTrackingLiveData;->n:Landroidx/room/InvalidationTracker$Observer;
    invoke-virtual {p1, v2}, Landroidx/room/InvalidationTracker;->addWeakObserver(Landroidx/room/InvalidationTracker$Observer;)V

    :outer_header
    iget-object p1, p0, Landroidx/room/RoomTrackingLiveData;->p:Ljava/util/concurrent/atomic/AtomicBoolean;
    invoke-virtual {p1, v3, v4}, Ljava/util/concurrent/atomic/AtomicBoolean;->compareAndSet(ZZ)Z
    move-result p1
    if-eqz p1, :not_computing

    const/4 p1, 0x0
    move v2, v3

    :drain_header
    :try_start_drain
    iget-object v5, p0, Landroidx/room/RoomTrackingLiveData;->o:Ljava/util/concurrent/atomic/AtomicBoolean;
    invoke-virtual {v5, v4, v3}, Ljava/util/concurrent/atomic/AtomicBoolean;->compareAndSet(ZZ)Z
    move-result v5
    :try_end_drain
    .catchall {:try_start_drain .. :try_end_drain} :catchall_compute
    if-eqz v5, :drain_done

    :try_start_compute
    iput v4, v0, Landroidx/room/RoomTrackingLiveData$refresh$1;->I$0:I
    iput v4, v0, Landroidx/room/RoomTrackingLiveData$refresh$1;->label:I
    invoke-virtual {p0, v0}, Landroidx/room/RoomTrackingLiveData;->compute(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    :try_end_compute
    .catch Ljava/lang/Exception; {:try_start_compute .. :try_end_compute} :catch_compute
    .catchall {:try_start_compute .. :try_end_compute} :catchall_compute

    if-ne p1, v1, :computed
    return-object v1

    :computed
    move v2, v4
    goto :drain_header

    :wrap_exception
    :try_start_wrap
    new-instance v0, Ljava/lang/RuntimeException;
    const-string v1, "Exception while computing database live data."
    invoke-direct {v0, v1, p1}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V
    throw v0

    :drain_done
    if-eqz v2, :release_computing
    invoke-virtual {p0, p1}, Landroidx/lifecycle/LiveData;->postValue(Ljava/lang/Object;)V
    :try_end_wrap
    .catchall {:try_start_wrap .. :try_end_wrap} :catchall_compute

    :release_computing
    iget-object p1, p0, Landroidx/room/RoomTrackingLiveData;->p:Ljava/util/concurrent/atomic/AtomicBoolean;
    invoke-virtual {p1, v3}, Ljava/util/concurrent/atomic/AtomicBoolean;->set(Z)V
    goto :retry_check

    :release_and_throw
    iget-object v0, p0, Landroidx/room/RoomTrackingLiveData;->p:Ljava/util/concurrent/atomic/AtomicBoolean;
    invoke-virtual {v0, v3}, Ljava/util/concurrent/atomic/AtomicBoolean;->set(Z)V
    throw p1

    :not_computing
    move v2, v3

    :retry_check
    if-eqz v2, :return_unit
    iget-object p1, p0, Landroidx/room/RoomTrackingLiveData;->o:Ljava/util/concurrent/atomic/AtomicBoolean;
    invoke-virtual {p1}, Ljava/util/concurrent/atomic/AtomicBoolean;->get()Z
    move-result p1
    if-nez p1, :outer_header

    :return_unit
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
.end method
