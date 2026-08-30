.class public final enum Lenums/TestEnumSharedSingletonInvokeArgs;
.super Ljava/lang/Enum;

.field private static final synthetic $VALUES:[Lenums/TestEnumSharedSingletonInvokeArgs;
.field public static final enum ONE:Lenums/TestEnumSharedSingletonInvokeArgs;
.field public static final enum TWO:Lenums/TestEnumSharedSingletonInvokeArgs;

.method static constructor <clinit>()V
    .registers 16

    const/4 v8, 0x1
    invoke-static {v8}, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;->format(I)Ljava/lang/String;
    move-result-object v8
    const-string v9, ""
    new-instance v10, Lenums/TestEnumSharedSingletonInvokeArgs$Holder;
    invoke-direct {v10, v8, v9}, Lenums/TestEnumSharedSingletonInvokeArgs$Holder;-><init>(Ljava/lang/String;Ljava/lang/String;)V
    const/4 v8, 0x2
    invoke-static {v8}, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;->format(I)Ljava/lang/String;
    move-result-object v11
    sget-object v8, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;->INSTANCE:Lenums/TestEnumSharedSingletonInvokeArgs$Helper;
    invoke-virtual {v8}, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;->getName()Ljava/lang/String;
    move-result-object v8
    filled-new-array {v8}, [Ljava/lang/Object;
    move-result-object v12
    const/4 v8, 0x3
    invoke-static {v8, v12}, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;->format(I[Ljava/lang/Object;)Ljava/lang/String;
    move-result-object v12
    new-instance v13, Lenums/TestEnumSharedSingletonInvokeArgs$Holder;
    invoke-direct {v13, v11, v12}, Lenums/TestEnumSharedSingletonInvokeArgs$Holder;-><init>(Ljava/lang/String;Ljava/lang/String;)V
    new-instance v0, Lenums/TestEnumSharedSingletonInvokeArgs;
    const-string v1, "ONE"
    const/4 v2, 0x0
    move-object v3, v10
    move-object v4, v13
    const/4 v5, 0x4
    const/4 v6, 0x5
    invoke-direct/range {v0 .. v6}, Lenums/TestEnumSharedSingletonInvokeArgs;-><init>(Ljava/lang/String;ILenums/TestEnumSharedSingletonInvokeArgs$Holder;Lenums/TestEnumSharedSingletonInvokeArgs$Holder;II)V
    sput-object v0, Lenums/TestEnumSharedSingletonInvokeArgs;->ONE:Lenums/TestEnumSharedSingletonInvokeArgs;

    const/4 v8, 0x6
    invoke-static {v8}, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;->format(I)Ljava/lang/String;
    move-result-object v8
    const-string v9, ""
    new-instance v10, Lenums/TestEnumSharedSingletonInvokeArgs$Holder;
    invoke-direct {v10, v8, v9}, Lenums/TestEnumSharedSingletonInvokeArgs$Holder;-><init>(Ljava/lang/String;Ljava/lang/String;)V
    const/4 v8, 0x7
    invoke-static {v8}, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;->format(I)Ljava/lang/String;
    move-result-object v11
    sget-object v8, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;->INSTANCE:Lenums/TestEnumSharedSingletonInvokeArgs$Helper;
    invoke-virtual {v8}, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;->getName()Ljava/lang/String;
    move-result-object v8
    filled-new-array {v8}, [Ljava/lang/Object;
    move-result-object v12
    const/16 v8, 0x8
    invoke-static {v8, v12}, Lenums/TestEnumSharedSingletonInvokeArgs$Helper;->format(I[Ljava/lang/Object;)Ljava/lang/String;
    move-result-object v12
    new-instance v13, Lenums/TestEnumSharedSingletonInvokeArgs$Holder;
    invoke-direct {v13, v11, v12}, Lenums/TestEnumSharedSingletonInvokeArgs$Holder;-><init>(Ljava/lang/String;Ljava/lang/String;)V
    new-instance v0, Lenums/TestEnumSharedSingletonInvokeArgs;
    const-string v1, "TWO"
    const/4 v2, 0x1
    move-object v3, v10
    move-object v4, v13
    const/16 v5, 0x9
    const/16 v6, 0xa
    invoke-direct/range {v0 .. v6}, Lenums/TestEnumSharedSingletonInvokeArgs;-><init>(Ljava/lang/String;ILenums/TestEnumSharedSingletonInvokeArgs$Holder;Lenums/TestEnumSharedSingletonInvokeArgs$Holder;II)V
    sput-object v0, Lenums/TestEnumSharedSingletonInvokeArgs;->TWO:Lenums/TestEnumSharedSingletonInvokeArgs;

    sget-object v0, Lenums/TestEnumSharedSingletonInvokeArgs;->ONE:Lenums/TestEnumSharedSingletonInvokeArgs;
    sget-object v1, Lenums/TestEnumSharedSingletonInvokeArgs;->TWO:Lenums/TestEnumSharedSingletonInvokeArgs;
    filled-new-array {v0, v1}, [Lenums/TestEnumSharedSingletonInvokeArgs;
    move-result-object v0
    sput-object v0, Lenums/TestEnumSharedSingletonInvokeArgs;->$VALUES:[Lenums/TestEnumSharedSingletonInvokeArgs;
    return-void
.end method

.method private constructor <init>(Ljava/lang/String;ILenums/TestEnumSharedSingletonInvokeArgs$Holder;Lenums/TestEnumSharedSingletonInvokeArgs$Holder;II)V
    .registers 7
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V
    return-void
.end method

.method public static values()[Lenums/TestEnumSharedSingletonInvokeArgs;
    .registers 1
    sget-object v0, Lenums/TestEnumSharedSingletonInvokeArgs;->$VALUES:[Lenums/TestEnumSharedSingletonInvokeArgs;
    invoke-virtual {v0}, [Lenums/TestEnumSharedSingletonInvokeArgs;->clone()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, [Lenums/TestEnumSharedSingletonInvokeArgs;
    return-object v0
.end method
