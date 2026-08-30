.class public Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;
.super Ljava/lang/Object;

.field private g:I
.field private h:J
.field private i:J
.field private j:J
.field private k:J
.field private l:J

.method private static f(Ljava/lang/Object;Z)Z
    .registers 2
    return p1
.end method

.method private g(IJJ)V
    .registers 6
    return-void
.end method

.method public declared-synchronized test(Ljava/lang/Object;Ljava/lang/Object;Z)V
    .registers 14

    monitor-enter p0

    :try_start_1
    invoke-static {p2, p3}, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->f(Ljava/lang/Object;Z)Z
    move-result p1
    :try_end_5
    .catchall {:try_start_1 .. :try_end_5} :catchall_72

    if-nez p1, :cond_9
    monitor-exit p0
    return-void

    :cond_9
    :try_start_9
    iget p1, p0, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->g:I
    const/4 p2, 0x1
    if-lez p1, :cond_10
    move p1, p2
    goto :goto_11

    :cond_10
    const/4 p1, 0x0

    :goto_11
    if-eqz p1, :cond_75
    const-wide/16 v0, 0x64
    iget-wide v2, p0, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->h:J
    sub-long v2, v0, v2
    long-to-int v5, v2
    iget-wide v2, p0, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->j:J
    int-to-long v6, v5
    add-long/2addr v2, v6
    iput-wide v2, p0, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->j:J
    iget-wide v2, p0, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->k:J
    iget-wide v6, p0, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->i:J
    add-long/2addr v2, v6
    iput-wide v2, p0, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->k:J
    if-lez v5, :cond_75
    iget-wide v2, p0, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->j:J
    :try_end_41
    .catchall {:try_start_9 .. :try_end_41} :catchall_72

    const-wide/16 v6, 0x7d0
    cmp-long p1, v2, v6
    if-gez p1, :cond_55

    :try_start_47
    iget-wide v2, p0, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->k:J
    :try_end_49
    .catchall {:try_start_47 .. :try_end_49} :catchall_51

    const-wide/32 v6, 0x80000
    cmp-long p1, v2, v6
    if-ltz p1, :cond_60
    goto :goto_55

    :catchall_51
    move-exception v0
    move-object p1, v0
    move-object v4, p0
    goto :goto_7d

    :cond_55
    :goto_55
    :try_start_55
    const-wide/16 v2, 0x2a
    iput-wide v2, p0, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->l:J

    :cond_60
    iget-wide v6, p0, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->i:J
    iget-wide v8, p0, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->l:J
    :try_end_64
    .catchall {:try_start_55 .. :try_end_64} :catchall_72

    move-object v4, p0

    :try_start_65
    invoke-virtual/range {v4 .. v9}, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->g(IJJ)V
    iput-wide v0, v4, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->h:J
    const-wide/16 v0, 0x0
    iput-wide v0, v4, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->i:J
    goto :goto_76

    :catchall_6f
    move-exception v0

    :goto_70
    move-object p1, v0
    goto :goto_7d

    :catchall_72
    move-exception v0
    move-object v4, p0
    goto :goto_70

    :cond_75
    move-object v4, p0

    :goto_76
    iget p1, v4, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->g:I
    sub-int/2addr p1, p2
    iput p1, v4, Lsynchronize/TestDeclaredSynchronizedAliasedHandlers;->g:I
    :try_end_7b
    .catchall {:try_start_65 .. :try_end_7b} :catchall_6f

    monitor-exit p0
    return-void

    :goto_7d
    :try_start_7d
    monitor-exit p0
    :try_end_7e
    .catchall {:try_start_7d .. :try_end_7e} :catchall_6f

    throw p1
.end method
