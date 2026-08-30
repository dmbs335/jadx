.class public final Lconditions/ObfuscatedContinuation;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;
.implements Lconditions/c;

.field public label:I

.method public constructor <init>(Lconditions/c;)V
    .registers 2

    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method protected invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 2

    return-object p1
.end method
