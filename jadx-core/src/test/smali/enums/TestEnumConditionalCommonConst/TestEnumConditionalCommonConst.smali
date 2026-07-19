.class public final enum Lenums/TestEnumConditionalCommonConst;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumConditionalCommonConst;
.field public static final enum ONE:Lenums/TestEnumConditionalCommonConst;
.field private final common:I
.field private final values:[Ljava/lang/String;

.method static constructor <clinit>()V
    .registers 12

    sget v0, Lenums/TestEnumConditionalCommonConstHelper;->SDK_INT:I
    const/16 v1, 0x1f
	const/4 v9, 0x2
    if-lt v0, v1, :old

	move v2, v9
    const/4 v3, 0x0
    new-array v4, v3, [Ljava/lang/String;
    goto :make_one

    :old
	move v2, v9
    const/4 v3, 0x1
    new-array v4, v3, [Ljava/lang/String;
    const/4 v3, 0x0
    const-string v5, "old"
    aput-object v5, v4, v3

    :make_one
    new-instance v6, Lenums/TestEnumConditionalCommonConst;
    const-string v10, "ONE"
    const/4 v11, 0x0
    invoke-direct {v6, v10, v11, v4, v2}, Lenums/TestEnumConditionalCommonConst;-><init>(Ljava/lang/String;I[Ljava/lang/String;I)V
    sput-object v6, Lenums/TestEnumConditionalCommonConst;->ONE:Lenums/TestEnumConditionalCommonConst;

    filled-new-array {v6}, [Lenums/TestEnumConditionalCommonConst;
    move-result-object v0
    sput-object v0, Lenums/TestEnumConditionalCommonConst;->$VALUES:[Lenums/TestEnumConditionalCommonConst;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I[Ljava/lang/String;I)V
    .registers 5
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    iput-object p3, p0, Lenums/TestEnumConditionalCommonConst;->values:[Ljava/lang/String;
    iput p4, p0, Lenums/TestEnumConditionalCommonConst;->common:I
    return-void
.end method

.method public static values()[Lenums/TestEnumConditionalCommonConst;
    .registers 1
    sget-object v0, Lenums/TestEnumConditionalCommonConst;->$VALUES:[Lenums/TestEnumConditionalCommonConst;
    invoke-virtual {v0}, Ljava/lang/Object;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumConditionalCommonConst;
    return-object v0
.end method
