.class public final Lconditions/TestCoroutineContinuationInit$OwnerContinuation;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field public label:I
.field final synthetic this$0:Lconditions/TestCoroutineContinuationInit;

.method public constructor <init>(Lconditions/TestCoroutineContinuationInit;Lkotlin/coroutines/Continuation;)V
    .registers 3

    iput-object p1, p0, Lconditions/TestCoroutineContinuationInit$OwnerContinuation;->this$0:Lconditions/TestCoroutineContinuationInit;
    invoke-direct {p0, p2}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method protected invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 2

    return-object p1
.end method
