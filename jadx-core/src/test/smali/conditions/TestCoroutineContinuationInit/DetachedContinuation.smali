.class public final Lconditions/DetachedContinuation;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field public label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2

    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method protected invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 2

    return-object p1
.end method
