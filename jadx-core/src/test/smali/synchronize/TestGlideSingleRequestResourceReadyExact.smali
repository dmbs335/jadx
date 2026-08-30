.class public Lsynchronize/TestGlideSingleRequestResourceReadyExact;
.super Ljava/lang/Object;

.field private engine:Lcom/bumptech/glide/load/engine/Engine;
.field private loadStatus:Lcom/bumptech/glide/load/engine/Engine$LoadStatus;
.field private requestLock:Ljava/lang/Object;
.field private resource:Lcom/bumptech/glide/load/engine/Resource;
.field private stateVerifier:Lcom/bumptech/glide/util/pool/StateVerifier;
.field private status:Lcom/bumptech/glide/request/SingleRequest$Status;
.field private transcodeClass:Ljava/lang/Class;

.method public onResourceReady(Lcom/bumptech/glide/load/engine/Resource;Lcom/bumptech/glide/load/DataSource;Z)V
    .registers 10
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lcom/bumptech/glide/load/engine/Resource<",
            "*>;",
            "Lcom/bumptech/glide/load/DataSource;",
            "Z)V"
        }
    .end annotation

    const-string v0, "Expected to receive an object of "

    const-string v1, "Expected to receive a Resource<R> with an object of "

    .line 527
    iget-object v2, p0, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->stateVerifier:Lcom/bumptech/glide/util/pool/StateVerifier;

    invoke-virtual {v2}, Lcom/bumptech/glide/util/pool/StateVerifier;->throwIfRecycled()V

    const/4 v2, 0x0

    .line 530
    :try_start_a
    iget-object v3, p0, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->requestLock:Ljava/lang/Object;

    monitor-enter v3
    :try_end_d
    .catchall {:try_start_a .. :try_end_d} :catchall_af

    .line 531
    :try_start_d
    iput-object v2, p0, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->loadStatus:Lcom/bumptech/glide/load/engine/Engine$LoadStatus;

    if-nez p1, :cond_2e

    .line 533
    new-instance p1, Lcom/bumptech/glide/load/engine/GlideException;

    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    iget-object p3, p0, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->transcodeClass:Ljava/lang/Class;

    invoke-virtual {p2, p3}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    const-string p3, " inside, but instead got null."

    invoke-virtual {p2, p3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    invoke-direct {p1, p2}, Lcom/bumptech/glide/load/engine/GlideException;-><init>(Ljava/lang/String;)V

    .line 539
    invoke-virtual {p0, p1}, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->onLoadFailed(Lcom/bumptech/glide/load/engine/GlideException;)V

    .line 540
    monitor-exit v3

    return-void

    .line 543
    :cond_2e
    invoke-interface {p1}, Lcom/bumptech/glide/load/engine/Resource;->get()Ljava/lang/Object;

    move-result-object v1

    if-eqz v1, :cond_5b

    .line 544
    iget-object v4, p0, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->transcodeClass:Ljava/lang/Class;

    invoke-virtual {v1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v5

    invoke-virtual {v4, v5}, Ljava/lang/Class;->isAssignableFrom(Ljava/lang/Class;)Z

    move-result v4

    if-nez v4, :cond_41

    goto :goto_5b

    .line 570
    :cond_41
    invoke-direct {p0}, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->canSetResource()Z

    move-result v0
    :try_end_45
    .catchall {:try_start_d .. :try_end_45} :catchall_ac

    if-nez v0, :cond_56

    .line 572
    :try_start_47
    iput-object v2, p0, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->resource:Lcom/bumptech/glide/load/engine/Resource;

    .line 574
    sget-object p2, Lcom/bumptech/glide/request/SingleRequest$Status;->COMPLETE:Lcom/bumptech/glide/request/SingleRequest$Status;

    iput-object p2, p0, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->status:Lcom/bumptech/glide/request/SingleRequest$Status;

    .line 575
    monitor-exit v3
    :try_end_4e
    .catchall {:try_start_47 .. :try_end_4e} :catchall_a8

    if-eqz p1, :cond_a7

    .line 583
    :goto_50
    iget-object p2, p0, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->engine:Lcom/bumptech/glide/load/engine/Engine;

    invoke-virtual {p2, p1}, Lcom/bumptech/glide/load/engine/Engine;->release(Lcom/bumptech/glide/load/engine/Resource;)V

    return-void

    .line 578
    :cond_56
    :try_start_56
    invoke-direct {p0, p1, v1, p2, p3}, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->onResourceReady(Lcom/bumptech/glide/load/engine/Resource;Ljava/lang/Object;Lcom/bumptech/glide/load/DataSource;Z)V

    .line 580
    monitor-exit v3
    :try_end_5a
    .catchall {:try_start_56 .. :try_end_5a} :catchall_ac

    return-void

    .line 546
    :cond_5b
    :goto_5b
    :try_start_5b
    iput-object v2, p0, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->resource:Lcom/bumptech/glide/load/engine/Resource;

    .line 547
    new-instance p2, Lcom/bumptech/glide/load/engine/GlideException;

    new-instance p3, Ljava/lang/StringBuilder;

    invoke-direct {p3, v0}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    iget-object v0, p0, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->transcodeClass:Ljava/lang/Class;

    invoke-virtual {p3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    const-string v0, " but instead got "

    invoke-virtual {p3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    if-eqz v1, :cond_75

    .line 553
    invoke-virtual {v1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v0

    goto :goto_77

    :cond_75
    const-string v0, ""

    :goto_77
    invoke-virtual {p3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    const-string v0, "{"

    invoke-virtual {p3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    const-string v0, "} inside Resource{"

    invoke-virtual {p3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p3, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    const-string v0, "}."

    invoke-virtual {p3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    if-eqz v1, :cond_94

    .line 562
    const-string v0, ""

    goto :goto_96

    .line 563
    :cond_94
    const-string v0, " To indicate failure return a null Resource object, rather than a Resource object containing null data."

    :goto_96
    invoke-virtual {p3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p3

    invoke-direct {p2, p3}, Lcom/bumptech/glide/load/engine/GlideException;-><init>(Ljava/lang/String;)V

    .line 566
    invoke-virtual {p0, p2}, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->onLoadFailed(Lcom/bumptech/glide/load/engine/GlideException;)V

    .line 567
    monitor-exit v3
    :try_end_a4
    .catchall {:try_start_5b .. :try_end_a4} :catchall_a8

    if-eqz p1, :cond_a7

    goto :goto_50

    :cond_a7
    return-void

    :catchall_a8
    move-exception p2

    move-object v2, p1

    move-object p1, p2

    goto :goto_ad

    :catchall_ac
    move-exception p1

    .line 580
    :goto_ad
    :try_start_ad
    monitor-exit v3
    :try_end_ae
    .catchall {:try_start_ad .. :try_end_ae} :catchall_ac

    :try_start_ae
    throw p1
    :try_end_af
    .catchall {:try_start_ae .. :try_end_af} :catchall_af

    :catchall_af
    move-exception p1

    if-eqz v2, :cond_b7

    .line 583
    iget-object p2, p0, Lsynchronize/TestGlideSingleRequestResourceReadyExact;->engine:Lcom/bumptech/glide/load/engine/Engine;

    invoke-virtual {p2, v2}, Lcom/bumptech/glide/load/engine/Engine;->release(Lcom/bumptech/glide/load/engine/Resource;)V

    :cond_b7
    throw p1
.end method
