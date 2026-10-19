.class public Lconditions/TestCmpOperandEvaluation;
.super Ljava/lang/Object;

.field private static calls:I

.method public constructor <init>()V
    .locals 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method private static nextUp()F
    .locals 1
    sget v0, Lconditions/TestCmpOperandEvaluation;->calls:I
    add-int/lit8 v0, v0, 0x1
    sput v0, Lconditions/TestCmpOperandEvaluation;->calls:I
    int-to-float v0, v0
    return v0
.end method

.method private static nextDown()F
    .locals 1
    sget v0, Lconditions/TestCmpOperandEvaluation;->calls:I
    add-int/lit8 v0, v0, 0x1
    sput v0, Lconditions/TestCmpOperandEvaluation;->calls:I
    neg-int v0, v0
    int-to-float v0, v0
    return v0
.end method

.method private static nextUpLong()J
    .locals 2
    sget v0, Lconditions/TestCmpOperandEvaluation;->calls:I
    add-int/lit8 v0, v0, 0x1
    sput v0, Lconditions/TestCmpOperandEvaluation;->calls:I
    int-to-long v0, v0
    return-wide v0
.end method

.method public static cmpLess()I
    .locals 3
    const/4 v0, 0x0
    sput v0, Lconditions/TestCmpOperandEvaluation;->calls:I
    invoke-static {}, Lconditions/TestCmpOperandEvaluation;->nextUp()F
    move-result v0
    invoke-static {}, Lconditions/TestCmpOperandEvaluation;->nextUp()F
    move-result v1
    cmpl-float v2, v0, v1
    return v2
.end method

.method public static cmpGreater()I
    .locals 3
    const/4 v0, 0x0
    sput v0, Lconditions/TestCmpOperandEvaluation;->calls:I
    invoke-static {}, Lconditions/TestCmpOperandEvaluation;->nextDown()F
    move-result v0
    invoke-static {}, Lconditions/TestCmpOperandEvaluation;->nextDown()F
    move-result v1
    cmpg-float v2, v0, v1
    return v2
.end method

.method public static cmpLong()I
    .locals 5
    const/4 v0, 0x0
    sput v0, Lconditions/TestCmpOperandEvaluation;->calls:I
    invoke-static {}, Lconditions/TestCmpOperandEvaluation;->nextUpLong()J
    move-result-wide v0
    invoke-static {}, Lconditions/TestCmpOperandEvaluation;->nextUpLong()J
    move-result-wide v2
    cmp-long v4, v0, v2
    return v4
.end method

.method public check()V
    .locals 2
    invoke-static {}, Lconditions/TestCmpOperandEvaluation;->cmpLess()I
    move-result v0
    const/4 v1, -0x1
    if-ne v0, v1, :fail
    sget v0, Lconditions/TestCmpOperandEvaluation;->calls:I
    const/4 v1, 0x2
    if-ne v0, v1, :fail

    invoke-static {}, Lconditions/TestCmpOperandEvaluation;->cmpGreater()I
    move-result v0
    const/4 v1, 0x1
    if-ne v0, v1, :fail
    sget v0, Lconditions/TestCmpOperandEvaluation;->calls:I
    const/4 v1, 0x2
    if-ne v0, v1, :fail

    invoke-static {}, Lconditions/TestCmpOperandEvaluation;->cmpLong()I
    move-result v0
    const/4 v1, -0x1
    if-ne v0, v1, :fail
    sget v0, Lconditions/TestCmpOperandEvaluation;->calls:I
    const/4 v1, 0x2
    if-ne v0, v1, :fail
    return-void

    :fail
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0
.end method
