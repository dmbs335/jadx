.class final Lconditions/TestCoroutineResumeExitDecisionTail$Result;
.super Ljava/lang/Object;

.field private final success:Z

.method public constructor <init>(Z)V
    .registers 2
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-boolean p1, p0, Lconditions/TestCoroutineResumeExitDecisionTail$Result;->success:Z
    return-void
.end method

.method public final isSuccess()Z
    .registers 2
    iget-boolean v0, p0, Lconditions/TestCoroutineResumeExitDecisionTail$Result;->success:Z
    return v0
.end method
