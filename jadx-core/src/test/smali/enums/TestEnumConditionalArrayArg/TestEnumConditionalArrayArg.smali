.class public final enum Lenums/TestEnumConditionalArrayArg;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumConditionalArrayArg;
.field public static final enum ONE:Lenums/TestEnumConditionalArrayArg;
.field private final values:[Ljava/lang/String;

.method static constructor <clinit>()V
    .registers 8

    sget v0, Lenums/TestEnumConditionalArrayArgHelper;->SDK_INT:I
    const/16 v1, 0x21
    if-lt v0, v1, :old
    const/4 v2, 0x0
    new-array v3, v2, [Ljava/lang/String;
    goto :make

    :old
	const/16 v1, 0x1e
	if-lt v0, v1, :very_old
    const/4 v2, 0x1
    new-array v3, v2, [Ljava/lang/String;
    const/4 v2, 0x0
    const-string v4, "old"
    aput-object v4, v3, v2
	goto :make

	:very_old
	const/4 v2, 0x2
	new-array v3, v2, [Ljava/lang/String;
	const/4 v2, 0x0
	const-string v4, "very-old"
	aput-object v4, v3, v2
	const/4 v2, 0x1
	const-string v4, "legacy"
	aput-object v4, v3, v2

    :make
    new-instance v5, Lenums/TestEnumConditionalArrayArg;
    const-string v6, "ONE"
    const/4 v7, 0x0
    invoke-direct {v5, v6, v7, v3}, Lenums/TestEnumConditionalArrayArg;-><init>(Ljava/lang/String;I[Ljava/lang/String;)V
    sput-object v5, Lenums/TestEnumConditionalArrayArg;->ONE:Lenums/TestEnumConditionalArrayArg;

    sget-object v0, Lenums/TestEnumConditionalArrayArg;->ONE:Lenums/TestEnumConditionalArrayArg;
    filled-new-array {v0}, [Lenums/TestEnumConditionalArrayArg;
    move-result-object v0
    sput-object v0, Lenums/TestEnumConditionalArrayArg;->$VALUES:[Lenums/TestEnumConditionalArrayArg;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I[Ljava/lang/String;)V
    .registers 4
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    iput-object p3, p0, Lenums/TestEnumConditionalArrayArg;->values:[Ljava/lang/String;
    return-void
.end method

.method public static values()[Lenums/TestEnumConditionalArrayArg;
    .registers 1
    sget-object v0, Lenums/TestEnumConditionalArrayArg;->$VALUES:[Lenums/TestEnumConditionalArrayArg;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumConditionalArrayArg;
    return-object v0
.end method
