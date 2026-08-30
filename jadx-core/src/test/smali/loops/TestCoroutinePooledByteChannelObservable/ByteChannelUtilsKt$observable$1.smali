.class final Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;
.source "ByteChannelUtils.kt"

# interfaces
.implements Lkotlin/jvm/functions/Function2;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lio/ktor/client/utils/ByteChannelUtilsKt;->observable(Lio/ktor/utils/io/ByteReadChannel;Lkotlin/coroutines/CoroutineContext;Ljava/lang/Long;Lio/ktor/client/content/ProgressListener;)Lio/ktor/utils/io/ByteReadChannel;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = null
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Lkotlin/coroutines/jvm/internal/SuspendLambda;",
        "Lkotlin/jvm/functions/Function2<",
        "Lio/ktor/utils/io/WriterScope;",
        "Lkotlin/coroutines/Continuation<",
        "-",
        "Lkotlin/Unit;",
        ">;",
        "Ljava/lang/Object;",
        ">;"
    }
.end annotation

.annotation system Ldalvik/annotation/SourceDebugExtension;
    value = "SMAP\nByteChannelUtils.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ByteChannelUtils.kt\nio/ktor/client/utils/ByteChannelUtilsKt$observable$1\n+ 2 Pool.kt\nio/ktor/utils/io/pool/PoolKt\n*L\n1#1,35:1\n182#2,5:36\n*S KotlinDebug\n*F\n+ 1 ByteChannelUtils.kt\nio/ktor/client/utils/ByteChannelUtilsKt$observable$1\n*L\n19#1:36,5\n*E\n"
.end annotation

.annotation runtime Lkotlin/Metadata;
    d1 = {
        "\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"
    }
    d2 = {
        "<anonymous>",
        "",
        "Lio/ktor/utils/io/WriterScope;"
    }
    k = 0x3
    mv = {
        0x2,
        0x2,
        0x0
    }
    xi = 0x30
.end annotation

.annotation runtime Lkotlin/coroutines/jvm/internal/DebugMetadata;
    c = "io.ktor.client.utils.ByteChannelUtilsKt$observable$1"
    f = "ByteChannelUtils.kt"
    i = {
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x1,
        0x1,
        0x1,
        0x1,
        0x1,
        0x1,
        0x1,
        0x1,
        0x2,
        0x2,
        0x2,
        0x2,
        0x2,
        0x2,
        0x2,
        0x2,
        0x3,
        0x3,
        0x3,
        0x3,
        0x3,
        0x3,
        0x3,
        0x3
    }
    l = {
        0x16,
        0x18,
        0x1a,
        0x1f
    }
    m = "invokeSuspend"
    n = {
        "$this$writer",
        "$this$useInstance$iv",
        "instance$iv",
        "byteArray",
        "$i$f$useInstance",
        "$i$a$-useInstance-ByteChannelUtilsKt$observable$1$1",
        "bytesSend",
        "$this$writer",
        "$this$useInstance$iv",
        "instance$iv",
        "byteArray",
        "$i$f$useInstance",
        "$i$a$-useInstance-ByteChannelUtilsKt$observable$1$1",
        "bytesSend",
        "read",
        "$this$writer",
        "$this$useInstance$iv",
        "instance$iv",
        "byteArray",
        "$i$f$useInstance",
        "$i$a$-useInstance-ByteChannelUtilsKt$observable$1$1",
        "bytesSend",
        "read",
        "$this$writer",
        "$this$useInstance$iv",
        "instance$iv",
        "byteArray",
        "closedCause",
        "$i$f$useInstance",
        "$i$a$-useInstance-ByteChannelUtilsKt$observable$1$1",
        "bytesSend"
    }
    s = {
        "L$0",
        "L$1",
        "L$5",
        "L$6",
        "I$0",
        "I$1",
        "J$0",
        "L$0",
        "L$1",
        "L$5",
        "L$6",
        "I$0",
        "I$1",
        "J$0",
        "I$2",
        "L$0",
        "L$1",
        "L$5",
        "L$6",
        "I$0",
        "I$1",
        "J$0",
        "I$2",
        "L$0",
        "L$1",
        "L$2",
        "L$3",
        "L$4",
        "I$0",
        "I$1",
        "J$0"
    }
    v = 0x1
.end annotation

.annotation build Lkotlin/jvm/internal/SourceDebugExtension;
    value = {
        "SMAP\nByteChannelUtils.kt\nKotlin\n*S Kotlin\n*F\n+ 1 ByteChannelUtils.kt\nio/ktor/client/utils/ByteChannelUtilsKt$observable$1\n+ 2 Pool.kt\nio/ktor/utils/io/pool/PoolKt\n*L\n1#1,35:1\n182#2,5:36\n*S KotlinDebug\n*F\n+ 1 ByteChannelUtils.kt\nio/ktor/client/utils/ByteChannelUtilsKt$observable$1\n*L\n19#1:36,5\n*E\n"
    }
.end annotation


# instance fields
.field final synthetic $contentLength:Ljava/lang/Long;

.field final synthetic $listener:Lio/ktor/client/content/ProgressListener;

.field final synthetic $this_observable:Lio/ktor/utils/io/ByteReadChannel;

.field I$0:I

.field I$1:I

.field I$2:I

.field J$0:J

.field private synthetic L$0:Ljava/lang/Object;

.field L$1:Ljava/lang/Object;

.field L$2:Ljava/lang/Object;

.field L$3:Ljava/lang/Object;

.field L$4:Ljava/lang/Object;

.field L$5:Ljava/lang/Object;

.field L$6:Ljava/lang/Object;

.field label:I


# direct methods
.method public constructor <init>(Lio/ktor/utils/io/ByteReadChannel;Lio/ktor/client/content/ProgressListener;Ljava/lang/Long;Lkotlin/coroutines/Continuation;)V
    .registers 5
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lio/ktor/utils/io/ByteReadChannel;",
            "Lio/ktor/client/content/ProgressListener;",
            "Ljava/lang/Long;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;",
            ">;)V"
        }
    .end annotation

    iput-object p1, p0, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->$this_observable:Lio/ktor/utils/io/ByteReadChannel;

    iput-object p2, p0, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->$listener:Lio/ktor/client/content/ProgressListener;

    iput-object p3, p0, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->$contentLength:Ljava/lang/Long;

    const/4 p1, 0x2

    invoke-direct {p0, p1, p4}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V

    return-void
.end method


# virtual methods
.method public final create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;
    .registers 7
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/Object;",
            "Lkotlin/coroutines/Continuation<",
            "*>;)",
            "Lkotlin/coroutines/Continuation<",
            "Lkotlin/Unit;",
            ">;"
        }
    .end annotation

    new-instance v0, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;

    iget-object v1, p0, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->$this_observable:Lio/ktor/utils/io/ByteReadChannel;

    iget-object v2, p0, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->$listener:Lio/ktor/client/content/ProgressListener;

    iget-object v3, p0, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->$contentLength:Ljava/lang/Long;

    invoke-direct {v0, v1, v2, v3, p2}, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;-><init>(Lio/ktor/utils/io/ByteReadChannel;Lio/ktor/client/content/ProgressListener;Ljava/lang/Long;Lkotlin/coroutines/Continuation;)V

    iput-object p1, v0, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$0:Ljava/lang/Object;

    return-object v0
.end method

.method public final invoke(Lio/ktor/utils/io/WriterScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lio/ktor/utils/io/WriterScope;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lkotlin/Unit;",
            ">;)",
            "Ljava/lang/Object;"
        }
    .end annotation

    invoke-virtual {p0, p1, p2}, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;

    move-result-object p1

    check-cast p1, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;

    sget-object p2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    invoke-virtual {p1, p2}, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public bridge synthetic invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    check-cast p1, Lio/ktor/utils/io/WriterScope;

    check-cast p2, Lkotlin/coroutines/Continuation;

    invoke-virtual {p0, p1, p2}, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->invoke(Lio/ktor/utils/io/WriterScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 28

    move-object/from16 v4, p0

    iget-object v0, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$0:Ljava/lang/Object;

    check-cast v0, Lio/ktor/utils/io/WriterScope;

    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    move-result-object v7

    iget v1, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->label:I

    const/4 v10, 0x4

    const/4 v11, 0x3

    const/4 v12, 0x2

    const/4 v13, 0x1

    if-eqz v1, :cond_da

    if-eq v1, v13, :cond_a7

    if-eq v1, v12, :cond_77

    if-eq v1, v11, :cond_3e

    if-ne v1, v10, :cond_31

    iget-object v0, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$4:Ljava/lang/Object;

    check-cast v0, Ljava/lang/Throwable;

    iget-object v0, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$3:Ljava/lang/Object;

    check-cast v0, [B

    iget-object v1, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$2:Ljava/lang/Object;

    iget-object v0, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$1:Ljava/lang/Object;

    move-object v2, v0

    check-cast v2, Lio/ktor/utils/io/pool/ObjectPool;

    :try_start_29
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_2c
    .catchall {:try_start_29 .. :try_end_2c} :catchall_2e

    goto/16 :goto_1eb

    :catchall_2e
    move-exception v0

    goto/16 :goto_1f3

    :cond_31
    new-instance v0, Ljava/lang/IllegalStateException;

    const v1, 0x624cfed3

    invoke-static {v1}, Lfixtures/obfuscation/StringDecoder;->decode(I)Ljava/lang/String;

    move-result-object v1

    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v0

    :cond_3e
    iget-wide v1, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->J$0:J

    iget v3, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$1:I

    iget v5, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$0:I

    iget-object v6, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$6:Ljava/lang/Object;

    check-cast v6, [B

    iget-object v15, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$5:Ljava/lang/Object;

    const-wide/16 v16, 0x0

    iget-object v8, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$4:Ljava/lang/Object;

    check-cast v8, Ljava/lang/Long;

    iget-object v9, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$3:Ljava/lang/Object;

    check-cast v9, Lio/ktor/client/content/ProgressListener;

    iget-object v10, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$2:Ljava/lang/Object;

    check-cast v10, Lio/ktor/utils/io/ByteReadChannel;

    iget-object v11, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$1:Ljava/lang/Object;

    check-cast v11, Lio/ktor/utils/io/pool/ObjectPool;

    :try_start_5c
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_5f
    .catchall {:try_start_5c .. :try_end_5f} :catchall_72

    move v14, v5

    const/4 v12, 0x3

    move-object/from16 v23, v8

    move-object v8, v0

    move-object v0, v10

    move-object v10, v11

    move v11, v3

    move-wide/from16 v24, v1

    move-object v1, v6

    move-wide/from16 v5, v24

    move-object v2, v9

    move-object v9, v15

    move-object/from16 v15, v23

    goto/16 :goto_19a

    :catchall_72
    move-exception v0

    move-object v2, v11

    move-object v1, v15

    goto/16 :goto_1f3

    :cond_77
    const-wide/16 v16, 0x0

    iget v1, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$2:I

    iget-wide v2, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->J$0:J

    iget v5, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$1:I

    iget v6, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$0:I

    iget-object v8, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$6:Ljava/lang/Object;

    check-cast v8, [B

    iget-object v9, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$5:Ljava/lang/Object;

    iget-object v10, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$4:Ljava/lang/Object;

    check-cast v10, Ljava/lang/Long;

    iget-object v11, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$3:Ljava/lang/Object;

    check-cast v11, Lio/ktor/client/content/ProgressListener;

    iget-object v15, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$2:Ljava/lang/Object;

    check-cast v15, Lio/ktor/utils/io/ByteReadChannel;

    iget-object v14, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$1:Ljava/lang/Object;

    check-cast v14, Lio/ktor/utils/io/pool/ObjectPool;

    :try_start_97
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_9a
    .catchall {:try_start_97 .. :try_end_9a} :catchall_a2

    move-object/from16 v23, v15

    move-object v15, v10

    move-object v10, v11

    move-object/from16 v11, v23

    goto/16 :goto_16c

    :catchall_a2
    move-exception v0

    move-object v1, v9

    :goto_a4
    move-object v2, v14

    goto/16 :goto_1f3

    :cond_a7
    const-wide/16 v16, 0x0

    iget-wide v1, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->J$0:J

    iget v3, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$1:I

    iget v5, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$0:I

    iget-object v6, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$6:Ljava/lang/Object;

    check-cast v6, [B

    iget-object v8, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$5:Ljava/lang/Object;

    iget-object v9, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$4:Ljava/lang/Object;

    check-cast v9, Ljava/lang/Long;

    iget-object v10, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$3:Ljava/lang/Object;

    check-cast v10, Lio/ktor/client/content/ProgressListener;

    iget-object v11, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$2:Ljava/lang/Object;

    check-cast v11, Lio/ktor/utils/io/ByteReadChannel;

    iget-object v14, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$1:Ljava/lang/Object;

    check-cast v14, Lio/ktor/utils/io/pool/ObjectPool;

    :try_start_c5
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_c8
    .catchall {:try_start_c5 .. :try_end_c8} :catchall_d7

    move-object v15, v9

    move-object v9, v8

    move-object v8, v0

    move-object v0, v11

    move v11, v3

    move v3, v5

    move-wide/from16 v23, v1

    move-object/from16 v2, p1

    move-object v1, v6

    move-wide/from16 v5, v23

    goto/16 :goto_132

    :catchall_d7
    move-exception v0

    move-object v1, v8

    goto :goto_a4

    :cond_da
    const-wide/16 v16, 0x0

    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    invoke-static {}, Lio/ktor/utils/io/pool/ByteArrayPoolKt;->getByteArrayPool()Lio/ktor/utils/io/pool/ObjectPool;

    move-result-object v2

    iget-object v1, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->$this_observable:Lio/ktor/utils/io/ByteReadChannel;

    iget-object v3, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->$listener:Lio/ktor/client/content/ProgressListener;

    iget-object v5, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->$contentLength:Ljava/lang/Long;

    invoke-interface {v2}, Lio/ktor/utils/io/pool/ObjectPool;->borrow()Ljava/lang/Object;

    move-result-object v6

    :try_start_ed
    move-object v8, v6

    check-cast v8, [B
    :try_end_f0
    .catchall {:try_start_ed .. :try_end_f0} :catchall_1f1

    move-object v9, v8

    move-object v8, v0

    move-object v0, v1

    move-object v1, v9

    move-object v10, v2

    move-object v2, v3

    move-object v15, v5

    move-object v9, v6

    move-wide/from16 v5, v16

    const/4 v11, 0x0

    const/4 v14, 0x0

    :goto_fc
    :try_start_fc
    invoke-interface {v0}, Lio/ktor/utils/io/ByteReadChannel;->isClosedForRead()Z

    move-result v3

    if-nez v3, :cond_1a8

    iput-object v8, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$0:Ljava/lang/Object;

    iput-object v10, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$1:Ljava/lang/Object;

    iput-object v0, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$2:Ljava/lang/Object;

    iput-object v2, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$3:Ljava/lang/Object;

    iput-object v15, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$4:Ljava/lang/Object;

    iput-object v9, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$5:Ljava/lang/Object;

    iput-object v1, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$6:Ljava/lang/Object;

    iput v14, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$0:I

    iput v11, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$1:I

    iput-wide v5, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->J$0:J

    iput v13, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->label:I

    move-object v3, v2

    const/4 v2, 0x0

    move-object/from16 v18, v3

    const/4 v3, 0x0

    move-wide/from16 v19, v5

    const/4 v5, 0x6

    const/4 v6, 0x0

    move-object/from16 v13, v18

    move-wide/from16 v21, v19

    invoke-static/range {v0 .. v6}, Lio/ktor/utils/io/ByteReadChannelOperationsKt;->readAvailable$default(Lio/ktor/utils/io/ByteReadChannel;[BIILkotlin/coroutines/Continuation;ILjava/lang/Object;)Ljava/lang/Object;

    move-result-object v2
    :try_end_129
    .catchall {:try_start_fc .. :try_end_129} :catchall_1a4

    if-ne v2, v7, :cond_12d

    goto/16 :goto_1e8

    :cond_12d
    move v3, v14

    move-wide/from16 v5, v21

    move-object v14, v10

    move-object v10, v13

    :goto_132
    :try_start_132
    check-cast v2, Ljava/lang/Number;

    invoke-virtual {v2}, Ljava/lang/Number;->intValue()I

    move-result v2

    if-lez v2, :cond_19e

    invoke-virtual {v8}, Lio/ktor/utils/io/WriterScope;->getChannel()Lio/ktor/utils/io/ByteWriteChannel;

    move-result-object v13

    iput-object v8, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$0:Ljava/lang/Object;

    iput-object v14, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$1:Ljava/lang/Object;

    iput-object v0, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$2:Ljava/lang/Object;

    iput-object v10, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$3:Ljava/lang/Object;

    iput-object v15, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$4:Ljava/lang/Object;

    iput-object v9, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$5:Ljava/lang/Object;

    iput-object v1, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$6:Ljava/lang/Object;

    iput v3, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$0:I

    iput v11, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$1:I

    iput-wide v5, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->J$0:J

    iput v2, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$2:I

    iput v12, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->label:I

    const/4 v12, 0x0

    invoke-static {v13, v1, v12, v2, v4}, Lio/ktor/utils/io/ByteWriteChannelOperationsKt;->writeFully(Lio/ktor/utils/io/ByteWriteChannel;[BIILkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object v13

    if-ne v13, v7, :cond_15f

    goto/16 :goto_1e8

    :cond_15f
    move/from16 v23, v11

    move-object v11, v0

    move-object v0, v8

    move-object v8, v1

    move v1, v2

    move-wide/from16 v24, v5

    move v6, v3

    move-wide/from16 v2, v24

    move/from16 v5, v23

    :goto_16c
    int-to-long v12, v1

    add-long/2addr v2, v12

    iput-object v0, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$0:Ljava/lang/Object;

    iput-object v14, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$1:Ljava/lang/Object;

    iput-object v11, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$2:Ljava/lang/Object;

    iput-object v10, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$3:Ljava/lang/Object;

    iput-object v15, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$4:Ljava/lang/Object;

    iput-object v9, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$5:Ljava/lang/Object;

    iput-object v8, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$6:Ljava/lang/Object;

    iput v6, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$0:I

    iput v5, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$1:I

    iput-wide v2, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->J$0:J

    iput v1, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$2:I

    const/4 v12, 0x3

    iput v12, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->label:I

    invoke-interface {v10, v2, v3, v15, v4}, Lio/ktor/client/content/ProgressListener;->onProgress(JLjava/lang/Long;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object v1
    :try_end_18b
    .catchall {:try_start_132 .. :try_end_18b} :catchall_a2

    if-ne v1, v7, :cond_18f

    goto/16 :goto_1e8

    :cond_18f
    move-object v1, v8

    move-object v8, v0

    move-object v0, v11

    move v11, v5

    move-object/from16 v23, v14

    move v14, v6

    move-wide v5, v2

    move-object v2, v10

    move-object/from16 v10, v23

    :goto_19a
    const/4 v12, 0x2

    const/4 v13, 0x1

    goto/16 :goto_fc

    :cond_19e
    move-object v2, v10

    move-object v10, v14

    const/4 v13, 0x1

    move v14, v3

    goto/16 :goto_fc

    :catchall_1a4
    move-exception v0

    move-object v1, v9

    move-object v2, v10

    goto :goto_1f3

    :cond_1a8
    move-object v13, v2

    move-wide/from16 v21, v5

    :try_start_1ab
    invoke-interface {v0}, Lio/ktor/utils/io/ByteReadChannel;->getClosedCause()Ljava/lang/Throwable;

    move-result-object v0

    invoke-virtual {v8}, Lio/ktor/utils/io/WriterScope;->getChannel()Lio/ktor/utils/io/ByteWriteChannel;

    move-result-object v2

    invoke-static {v2, v0}, Lio/ktor/utils/io/ByteWriteChannelOperationsKt;->close(Lio/ktor/utils/io/ByteWriteChannel;Ljava/lang/Throwable;)V

    if-nez v0, :cond_1e9

    move-wide/from16 v5, v21

    cmp-long v2, v5, v16

    if-nez v2, :cond_1e9

    invoke-static {v8}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v2

    iput-object v2, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$0:Ljava/lang/Object;

    iput-object v10, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$1:Ljava/lang/Object;

    iput-object v9, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$2:Ljava/lang/Object;

    invoke-static {v1}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v1

    iput-object v1, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$3:Ljava/lang/Object;

    invoke-static {v0}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v0

    iput-object v0, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$4:Ljava/lang/Object;

    const/4 v0, 0x0

    iput-object v0, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$5:Ljava/lang/Object;

    iput-object v0, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->L$6:Ljava/lang/Object;

    iput v14, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$0:I

    iput v11, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->I$1:I

    iput-wide v5, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->J$0:J

    const/4 v0, 0x4

    iput v0, v4, Lio/ktor/client/utils/ByteChannelUtilsKt$observable$1;->label:I

    invoke-interface {v13, v5, v6, v15, v4}, Lio/ktor/client/content/ProgressListener;->onProgress(JLjava/lang/Long;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object v0
    :try_end_1e6
    .catchall {:try_start_1ab .. :try_end_1e6} :catchall_1a4

    if-ne v0, v7, :cond_1e9

    :goto_1e8
    return-object v7

    :cond_1e9
    move-object v1, v9

    move-object v2, v10

    :goto_1eb
    :try_start_1eb
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end_1ed
    .catchall {:try_start_1eb .. :try_end_1ed} :catchall_2e

    invoke-interface {v2, v1}, Lio/ktor/utils/io/pool/ObjectPool;->recycle(Ljava/lang/Object;)V

    return-object v0

    :catchall_1f1
    move-exception v0

    move-object v1, v6

    :goto_1f3
    invoke-interface {v2, v1}, Lio/ktor/utils/io/pool/ObjectPool;->recycle(Ljava/lang/Object;)V

    throw v0
.end method
