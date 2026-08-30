.class public final Landroidx/compose/foundation/gestures/UpdatableAnimationState;
.super Ljava/lang/Object;

.field private static final Companion:Landroidx/compose/foundation/gestures/UpdatableAnimationState$Companion;
.field private static final ZeroVector:Landroidx/compose/animation/core/AnimationVector1D;
.field public b:J
.field public c:Landroidx/compose/animation/core/AnimationVector1D;
.field public d:Z
.field public e:F

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public final animateToZero(Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 15

    instance-of v0, p3, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;
    if-eqz v0, :cond_13
    move-object v0, p3
    check-cast v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;
    iget v1, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :cond_13
    sub-int/2addr v1, v2
    iput v1, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->label:I
    goto :goto_18

    :cond_13
    new-instance v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;
    invoke-direct {v0, p0, p3}, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;-><init>(Landroidx/compose/foundation/gestures/UpdatableAnimationState;Lkotlin/coroutines/Continuation;)V

    :goto_18
    iget-object p3, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->label:I
    const/4 v3, 0x0
    const-wide/high16 v4, -0x8000000000000000L
    const/4 v6, 0x0
    const/4 v7, 0x2
    const/4 v8, 0x1
    if-eqz v2, :cond_50
    if-eq v2, v8, :cond_40
    if-ne v2, v7, :cond_38
    iget-object p1, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->L$0:Ljava/lang/Object;
    check-cast p1, Lkotlin/jvm/functions/Function0;

    :try_start_30
    invoke-static {p3}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_33
    .catchall {:try_start_30 .. :try_end_33} :catchall_35
    goto/16 :goto_bb

    :catchall_35
    move-exception p1
    goto/16 :goto_c9

    :cond_38
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string p2, "call to \'resume\' before \'invoke\' with coroutine"
    invoke-direct {p1, p2}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1

    :cond_40
    iget p1, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->F$0:F
    iget-object p2, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->L$1:Ljava/lang/Object;
    check-cast p2, Lkotlin/jvm/functions/Function0;
    iget-object v2, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->L$0:Ljava/lang/Object;
    check-cast v2, Lkotlin/jvm/functions/Function1;

    :try_start_4a
    invoke-static {p3}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_4d
    .catchall {:try_start_4a .. :try_end_4d} :catchall_35
    move-object p3, p2
    move-object p2, v2
    goto :goto_95

    :cond_50
    invoke-static {p3}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-boolean p3, p0, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->d:Z
    if-eqz p3, :cond_5c
    const-string p3, "animateToZero called while previous animation is running"
    invoke-static {p3}, Landroidx/compose/foundation/internal/InlineClassHelperKt;->throwIllegalStateException(Ljava/lang/String;)V

    :cond_5c
    invoke-interface {v0}, Lkotlin/coroutines/Continuation;->getContext()Lkotlin/coroutines/CoroutineContext;
    move-result-object p3
    sget-object v2, Landroidx/compose/ui/MotionDurationScale;->Key:Landroidx/compose/ui/MotionDurationScale$Key;
    invoke-interface {p3, v2}, Lkotlin/coroutines/CoroutineContext;->get(Lkotlin/coroutines/CoroutineContext$a;)Lkotlin/coroutines/CoroutineContext$Element;
    move-result-object p3
    check-cast p3, Landroidx/compose/ui/MotionDurationScale;
    if-eqz p3, :cond_6f
    invoke-interface {p3}, Landroidx/compose/ui/MotionDurationScale;->getScaleFactor()F
    move-result p3
    goto :goto_71

    :cond_6f
    const/high16 p3, 0x3f800000

    :goto_71
    iput-boolean v8, p0, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->d:Z
    move-object v10, p2
    move-object p2, p1
    move p1, p3
    move-object p3, v10

    :cond_77
    :try_start_77
    sget-object v2, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->Companion:Landroidx/compose/foundation/gestures/UpdatableAnimationState$Companion;
    iget v9, p0, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->e:F
    invoke-virtual {v2, v9}, Landroidx/compose/foundation/gestures/UpdatableAnimationState$Companion;->isZeroish(F)Z
    move-result v2
    if-nez v2, :cond_9c
    new-instance v2, Landroidx/compose/foundation/gestures/v2;
    invoke-direct {v2, p0, p1, p2}, Landroidx/compose/foundation/gestures/v2;-><init>(Landroidx/compose/foundation/gestures/UpdatableAnimationState;FLkotlin/jvm/functions/Function1;)V
    iput-object p2, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->L$0:Ljava/lang/Object;
    iput-object p3, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->L$1:Ljava/lang/Object;
    iput p1, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->F$0:F
    iput v8, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->label:I
    invoke-static {v2, v0}, Landroidx/compose/runtime/MonotonicFrameClockKt;->withFrameNanos(Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-ne v2, v1, :cond_95
    goto :goto_ba

    :cond_95
    :goto_95
    invoke-interface {p3}, Lkotlin/jvm/functions/Function0;->invoke()Ljava/lang/Object;
    cmpg-float v2, p1, v6
    if-nez v2, :cond_77

    :cond_9c
    move-object p1, p3
    iget p3, p0, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->e:F
    invoke-static {p3}, Ljava/lang/Math;->abs(F)F
    move-result p3
    cmpg-float p3, p3, v6
    if-nez p3, :cond_a8
    goto :goto_be

    :cond_a8
    new-instance p3, Landroidx/compose/foundation/gestures/w2;
    invoke-direct {p3, p0, p2}, Landroidx/compose/foundation/gestures/w2;-><init>(Landroidx/compose/foundation/gestures/UpdatableAnimationState;Lkotlin/jvm/functions/Function1;)V
    iput-object p1, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->L$0:Ljava/lang/Object;
    const/4 p2, 0x0
    iput-object p2, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->L$1:Ljava/lang/Object;
    iput v7, v0, Landroidx/compose/foundation/gestures/UpdatableAnimationState$animateToZero$1;->label:I
    invoke-static {p3, v0}, Landroidx/compose/runtime/MonotonicFrameClockKt;->withFrameNanos(Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p2
    if-ne p2, v1, :cond_bb

    :goto_ba
    return-object v1

    :cond_bb
    :goto_bb
    invoke-interface {p1}, Lkotlin/jvm/functions/Function0;->invoke()Ljava/lang/Object;
    :try_end_be
    .catchall {:try_start_77 .. :try_end_be} :catchall_35

    :goto_be
    iput-wide v4, p0, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->b:J
    sget-object p1, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->ZeroVector:Landroidx/compose/animation/core/AnimationVector1D;
    iput-object p1, p0, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->c:Landroidx/compose/animation/core/AnimationVector1D;
    iput-boolean v3, p0, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->d:Z
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :goto_c9
    iput-wide v4, p0, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->b:J
    sget-object p2, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->ZeroVector:Landroidx/compose/animation/core/AnimationVector1D;
    iput-object p2, p0, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->c:Landroidx/compose/animation/core/AnimationVector1D;
    iput-boolean v3, p0, Landroidx/compose/foundation/gestures/UpdatableAnimationState;->d:Z
    throw p1
.end method
