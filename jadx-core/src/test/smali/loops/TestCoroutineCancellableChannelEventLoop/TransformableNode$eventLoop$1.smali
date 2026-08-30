.class public final Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;
.source "Transformable.kt"

.implements Lkotlin/jvm/functions/Function2;

.field private synthetic L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field L$2:Ljava/lang/Object;
.field label:I
.field final synthetic this$0:Landroidx/compose/foundation/gestures/TransformableNode;

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 11

    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0

    iget v1, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1
    if-eqz v1, :initial
    if-eq v1, v3, :resume_receive
    if-ne v1, v2, :invalid_state

    iget-object v1, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->L$0:Ljava/lang/Object;
    check-cast v1, Lkotlinx/coroutines/CoroutineScope;
    :try_start_resume_action
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume_action
    .catch Ljava/util/concurrent/CancellationException; {:try_start_resume_action .. :try_end_resume_action} :catch_resume_action

    :catch_resume_action
    move-object p1, v1
    goto :loop_header

    :invalid_state
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string v0, "call to 'resume' before 'invoke'"
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1

    :resume_receive
    iget-object v1, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->L$2:Ljava/lang/Object;
    check-cast v1, Lkotlin/jvm/internal/Ref$ObjectRef;
    iget-object v4, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->L$1:Ljava/lang/Object;
    check-cast v4, Lkotlin/jvm/internal/Ref$ObjectRef;
    iget-object v5, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->L$0:Ljava/lang/Object;
    check-cast v5, Lkotlinx/coroutines/CoroutineScope;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :consume_event

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object p1, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->L$0:Ljava/lang/Object;
    check-cast p1, Lkotlinx/coroutines/CoroutineScope;

    :loop_header
    invoke-static {p1}, Lkotlinx/coroutines/CoroutineScopeKt;->isActive(Lkotlinx/coroutines/CoroutineScope;)Z
    move-result v1
    if-eqz v1, :done

    new-instance v1, Lkotlin/jvm/internal/Ref$ObjectRef;
    invoke-direct {v1}, Lkotlin/jvm/internal/Ref$ObjectRef;-><init>()V
    iget-object v4, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->this$0:Landroidx/compose/foundation/gestures/TransformableNode;
    invoke-static {v4}, Landroidx/compose/foundation/gestures/TransformableNode;->access$getChannel$p(Landroidx/compose/foundation/gestures/TransformableNode;)Lkotlinx/coroutines/channels/Channel;
    move-result-object v4
    iput-object p1, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->L$0:Ljava/lang/Object;
    iput-object v1, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->L$1:Ljava/lang/Object;
    iput-object v1, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->L$2:Ljava/lang/Object;
    iput v3, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->label:I
    invoke-interface {v4, p0}, Lkotlinx/coroutines/channels/ReceiveChannel;->receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-ne v4, v0, :receive_immediate
    goto :suspended

    :receive_immediate
    move-object v5, p1
    move-object p1, v4
    move-object v4, v1

    :consume_event
    iput-object p1, v1, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;
    iget-object p1, v4, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;
    instance-of p1, p1, Landroidx/compose/foundation/gestures/TransformEvent$TransformStarted;
    if-eqz p1, :continue_loop

    :try_start_action
    iget-object p1, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->this$0:Landroidx/compose/foundation/gestures/TransformableNode;
    invoke-static {p1}, Landroidx/compose/foundation/gestures/TransformableNode;->access$getState$p(Landroidx/compose/foundation/gestures/TransformableNode;)Landroidx/compose/foundation/gestures/TransformableState;
    move-result-object p1
    sget-object v1, Landroidx/compose/foundation/MutatePriority;->UserInput:Landroidx/compose/foundation/MutatePriority;
    const/4 v6, 0x0
    iput-object v5, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->L$1:Ljava/lang/Object;
    iput-object v6, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->L$2:Ljava/lang/Object;
    iput v2, p0, Landroidx/compose/foundation/gestures/TransformableNode$eventLoop$1;->label:I
    invoke-interface {p1, v1, v6, p0}, Landroidx/compose/foundation/gestures/TransformableState;->transform(Landroidx/compose/foundation/MutatePriority;Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    :try_end_action
    .catch Ljava/util/concurrent/CancellationException; {:try_start_action .. :try_end_action} :catch_action

    if-ne p1, v0, :continue_loop

    :suspended
    return-object v0

    :catch_action
    :continue_loop
    move-object p1, v5
    goto :loop_header

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
.end method
