.class final Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;
.source "BarGraphResponse.kt"


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lconditions/TestCoroutineResumeMoveNullableAddTail;->mapToEntity(Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = null
.end annotation

.annotation runtime Lkotlin/Metadata;
    k = 0x3
    mv = {
        0x2,
        0x2,
        0x0
    }
    xi = 0x30
.end annotation

.annotation runtime Lkotlin/coroutines/jvm/internal/DebugMetadata;
    c = "fixtures.app.ktor.feed.resource.component.response.BarGraphResponseKt"
    f = "BarGraphResponse.kt"
    i = {
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0
    }
    l = {
        0x21
    }
    m = "mapToEntity"
    n = {
        "$this$mapToEntity",
        "graphId",
        "$this$mapNotNull$iv",
        "$this$mapNotNullTo$iv$iv",
        "destination$iv$iv",
        "$this$forEach$iv$iv$iv",
        "element$iv$iv$iv",
        "element$iv$iv",
        "it",
        "$i$a$-let-BarGraphResponseKt$mapToEntity$2",
        "$i$f$mapNotNull",
        "$i$f$mapNotNullTo",
        "$i$f$forEach",
        "$i$a$-forEach-CollectionsKt___CollectionsKt$mapNotNullTo$1$iv$iv",
        "$i$a$-mapNotNull-BarGraphResponseKt$mapToEntity$2$1"
    }
    s = {
        "L$0",
        "L$1",
        "L$2",
        "L$7",
        "L$8",
        "L$9",
        "L$11",
        "L$12",
        "L$13",
        "I$0",
        "I$1",
        "I$2",
        "I$3",
        "I$4",
        "I$5"
    }
    v = 0x1
.end annotation


# instance fields
.field I$0:I

.field I$1:I

.field I$2:I

.field I$3:I

.field I$4:I

.field I$5:I

.field L$0:Ljava/lang/Object;

.field L$1:Ljava/lang/Object;

.field L$10:Ljava/lang/Object;

.field L$11:Ljava/lang/Object;

.field L$12:Ljava/lang/Object;

.field L$13:Ljava/lang/Object;

.field L$2:Ljava/lang/Object;

.field L$3:Ljava/lang/Object;

.field L$4:Ljava/lang/Object;

.field L$5:Ljava/lang/Object;

.field L$6:Ljava/lang/Object;

.field L$7:Ljava/lang/Object;

.field L$8:Ljava/lang/Object;

.field L$9:Ljava/lang/Object;

.field label:I

.field synthetic result:Ljava/lang/Object;


# direct methods
.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;",
            ">;)V"
        }
    .end annotation

    .line 1
    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V

    .line 2
    .line 3
    .line 4
    return-void
.end method


# virtual methods
.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3
    .param p1    # Ljava/lang/Object;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .annotation build Lorg/jetbrains/annotations/Nullable;
    .end annotation

    .line 1
    iput-object p1, p0, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->result:Ljava/lang/Object;

    .line 2
    .line 3
    iget p1, p0, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->label:I

    .line 4
    .line 5
    const/high16 v0, -0x80000000

    .line 6
    .line 7
    or-int/2addr p1, v0

    .line 8
    iput p1, p0, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->label:I

    .line 9
    .line 10
    const/4 p1, 0x0

    .line 11
    invoke-static {p1, p0}, Lconditions/TestCoroutineResumeMoveNullableAddTail;->mapToEntity(Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 12
    .line 13
    .line 14
    move-result-object p1

    .line 15
    return-object p1
.end method
