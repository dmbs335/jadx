.class public Lothers/TestStaticEncodedIndirectRead;
.super Ljava/lang/Object;

.field public static INITIAL:I = 0x5
.field public static observed:I

.method static constructor <clinit>()V
    .registers 1

    invoke-static {}, Lothers/TestStaticEncodedIndirectRead;->readInitial()I
    move-result v0
    sput v0, Lothers/TestStaticEncodedIndirectRead;->observed:I
    const/16 v0, 0x9
    sput v0, Lothers/TestStaticEncodedIndirectRead;->INITIAL:I
    return-void
.end method

.method private static readInitial()I
    .registers 1

    sget v0, Lothers/TestStaticEncodedIndirectRead;->INITIAL:I
    return v0
.end method

.method public constructor <init>()V
    .registers 1

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public check()V
    .registers 2

    sget v0, Lothers/TestStaticEncodedIndirectRead;->observed:I
    const/4 v1, 0x5
    if-ne v0, v1, :fail
    sget v0, Lothers/TestStaticEncodedIndirectRead;->INITIAL:I
    const/16 v1, 0x9
    if-ne v0, v1, :fail
    return-void

    :fail
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0
.end method
