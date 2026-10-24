.class public Lothers/TestStaticCrossClassRead;
.super Ljava/lang/Object;

.field public static final INITIAL:I
.field public static observed:I

.method static constructor <clinit>()V
    .registers 1

    invoke-static {}, Lothers/StaticReadHelper;->read()I
    move-result v0
    sput v0, Lothers/TestStaticCrossClassRead;->observed:I
    const/16 v0, 0x9
    sput v0, Lothers/TestStaticCrossClassRead;->INITIAL:I
    return-void
.end method

.method public constructor <init>()V
    .registers 1

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public check()V
    .registers 2

    sget v0, Lothers/TestStaticCrossClassRead;->observed:I
    if-nez v0, :fail
    sget v0, Lothers/TestStaticCrossClassRead;->INITIAL:I
    const/16 v1, 0x9
    if-ne v0, v1, :fail
    return-void

    :fail
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0
.end method
