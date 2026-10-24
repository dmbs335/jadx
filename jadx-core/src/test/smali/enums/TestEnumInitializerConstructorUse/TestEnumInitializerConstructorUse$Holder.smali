.class public Lenums/TestEnumInitializerConstructorUse$Holder;
.super Ljava/lang/Object;

.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lenums/TestEnumInitializerConstructorUse;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x9
    name = "Holder"
.end annotation

.field public seen:Lenums/TestEnumInitializerConstructorUse;

.method public constructor <init>(Lenums/TestEnumInitializerConstructorUse;)V
    .registers 2
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lenums/TestEnumInitializerConstructorUse$Holder;->seen:Lenums/TestEnumInitializerConstructorUse;
    return-void
.end method
