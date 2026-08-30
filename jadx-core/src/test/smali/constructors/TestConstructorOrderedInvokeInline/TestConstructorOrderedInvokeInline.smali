.class public Lconstructors/TestConstructorOrderedInvokeInline;
.super Lconstructors/TestConstructorOrderedInvokeInlineParent;

.method private constructor <init>()V
    .locals 4

    const/4 v0, 0x1
    const/4 v1, 0x2
    filled-new-array {v0, v1}, [I
    move-result-object v0
    const/4 v1, 0x0
    const/4 v2, 0x1
    const/4 v3, 0x0
    invoke-static {v0, v1, v2, v3}, Lconstructors/ConstructorInvokeHelper;->join$default([IIILjava/lang/Object;)Ljava/lang/String;
    move-result-object v0
    const/16 v1, 0x1c
    invoke-direct {p0, v1, v0, v3}, Lconstructors/TestConstructorOrderedInvokeInlineParent;-><init>(ILjava/lang/String;Ljava/lang/Object;)V
    return-void
.end method
