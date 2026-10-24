.class public Lothers/TestStaticFinalConstructorRead;
.super Ljava/lang/Object;

.field public static final INITIAL:I
.field public static observed:I
.field public static reader:Lothers/TestStaticFinalConstructorRead;
.field private static initialized:Z

.method static constructor <clinit>()V
    .registers 1

    new-instance v0, Lothers/TestStaticFinalConstructorRead;
    invoke-direct {v0}, Lothers/TestStaticFinalConstructorRead;-><init>()V
    sput-object v0, Lothers/TestStaticFinalConstructorRead;->reader:Lothers/TestStaticFinalConstructorRead;
    const/16 v0, 0x9
    sput v0, Lothers/TestStaticFinalConstructorRead;->INITIAL:I
    const/4 v0, 0x1
    sput-boolean v0, Lothers/TestStaticFinalConstructorRead;->initialized:Z
    return-void
.end method

.method public constructor <init>()V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    sget-boolean v0, Lothers/TestStaticFinalConstructorRead;->initialized:Z
    if-nez v0, :done
    sget v0, Lothers/TestStaticFinalConstructorRead;->INITIAL:I
    sput v0, Lothers/TestStaticFinalConstructorRead;->observed:I
    :done
    return-void
.end method

.method public check()V
    .registers 2

    sget v0, Lothers/TestStaticFinalConstructorRead;->observed:I
    if-nez v0, :fail
    sget v0, Lothers/TestStaticFinalConstructorRead;->INITIAL:I
    const/16 v1, 0x9
    if-ne v0, v1, :fail
    return-void

    :fail
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0
.end method
