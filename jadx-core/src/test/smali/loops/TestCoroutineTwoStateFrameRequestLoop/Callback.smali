.class public final synthetic Lloops/Callback;
.super Ljava/lang/Object;
.implements Lkotlin/jvm/functions/Function1;

.field private final synthetic recomposer:Lloops/TestCoroutineTwoStateFrameRequestLoop;
.field private final synthetic toRecompose:Ljava/util/List;
.field private final synthetic toApply:Ljava/util/List;
.field private final synthetic signal:Landroidx/compose/runtime/ProduceFrameSignal;

.method public synthetic constructor <init>(Lloops/TestCoroutineTwoStateFrameRequestLoop;Ljava/util/List;Ljava/util/List;Landroidx/compose/runtime/ProduceFrameSignal;)V
    .registers 5

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lloops/Callback;->recomposer:Lloops/TestCoroutineTwoStateFrameRequestLoop;
    iput-object p2, p0, Lloops/Callback;->toRecompose:Ljava/util/List;
    iput-object p3, p0, Lloops/Callback;->toApply:Ljava/util/List;
    iput-object p4, p0, Lloops/Callback;->signal:Landroidx/compose/runtime/ProduceFrameSignal;
    return-void
.end method

.method public final invoke(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 6

    iget-object v0, p0, Lloops/Callback;->recomposer:Lloops/TestCoroutineTwoStateFrameRequestLoop;
    iget-object v1, p0, Lloops/Callback;->toRecompose:Ljava/util/List;
    iget-object v2, p0, Lloops/Callback;->toApply:Ljava/util/List;
    iget-object v3, p0, Lloops/Callback;->signal:Landroidx/compose/runtime/ProduceFrameSignal;
    invoke-static {v0, v1, v2, v3, p1}, Lloops/TestCoroutineTwoStateFrameRequestLoop;->onFrame(Lloops/TestCoroutineTwoStateFrameRequestLoop;Ljava/util/List;Ljava/util/List;Landroidx/compose/runtime/ProduceFrameSignal;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method
