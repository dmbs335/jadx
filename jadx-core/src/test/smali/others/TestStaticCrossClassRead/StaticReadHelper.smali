.class public Lothers/StaticReadHelper;
.super Ljava/lang/Object;

.method public static read()I
    .registers 1

    invoke-static {}, Lothers/StaticReadHelper;->readNested()I
    move-result v0
    return v0
.end method

.method private static readNested()I
    .registers 1

    sget v0, Lothers/TestStaticCrossClassRead;->INITIAL:I
    return v0
.end method
