.class public Lconstructors/TestConstructorFinalSingletonConditionalReadBlocked;
.super Lconstructors/TestConstructorFinalSingletonConditionalReadBlockedParent;

.method public constructor <init>()V
    .locals 2

    sget-object v0, Lconstructors/ConstructorFinalSingletonConditionalRead;->INSTANCE:Lconstructors/ConstructorFinalSingletonConditionalRead;
    invoke-static {}, Lconstructors/ConstructorConditionGuard;->isEnabled()Z
    move-result v1
    if-eqz v1, :false_value

    invoke-virtual {v0}, Lconstructors/ConstructorFinalSingletonConditionalRead;->isEnabled()Z
    move-result v1
	if-nez v1, :true_value

    invoke-virtual {v0}, Lconstructors/ConstructorFinalSingletonConditionalRead;->isOtherEnabled()Z
    move-result v1
    if-eqz v1, :false_value

	:true_value
    const/4 v1, 0x1
    goto :call_super

    :false_value
    const/4 v1, 0x0

    :call_super
    invoke-direct {p0, v1}, Lconstructors/TestConstructorFinalSingletonConditionalReadBlockedParent;-><init>(Z)V
    return-void
.end method
