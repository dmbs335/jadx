.class public Lconstructors/TestConstructorFinalSingletonConditionInline;
.super Lconstructors/TestConstructorFinalSingletonConditionInlineParent;

.method public constructor <init>()V
    .locals 2

    sget-object v0, Lconstructors/ConstructorFinalSingletonCondition;->INSTANCE:Lconstructors/ConstructorFinalSingletonCondition;
    invoke-virtual {v0}, Lconstructors/ConstructorFinalSingletonCondition;->isFirst()Z
    move-result v1
    if-eqz v1, :false_value

    invoke-virtual {v0}, Lconstructors/ConstructorFinalSingletonCondition;->isSecond()Z
    move-result v1
    if-eqz v1, :false_value

    const/4 v1, 0x1
    goto :call_super

    :false_value
    const/4 v1, 0x0

    :call_super
    invoke-direct {p0, v1}, Lconstructors/TestConstructorFinalSingletonConditionInlineParent;-><init>(Z)V
    return-void
.end method
