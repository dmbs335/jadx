.class public final Ltypes/TestSiblingFieldPhi$Idle;
.super Ltypes/TestSiblingFieldPhi$Status;

.annotation system Ldalvik/annotation/EnclosingClass;
    value = Ltypes/TestSiblingFieldPhi;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = "Idle"
.end annotation

.field public static final INSTANCE:Ltypes/TestSiblingFieldPhi$Idle;

.method static constructor <clinit>()V
    .registers 1
    new-instance v0, Ltypes/TestSiblingFieldPhi$Idle;
    invoke-direct {v0}, Ltypes/TestSiblingFieldPhi$Idle;-><init>()V
    sput-object v0, Ltypes/TestSiblingFieldPhi$Idle;->INSTANCE:Ltypes/TestSiblingFieldPhi$Idle;
    return-void
.end method

.method private constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ltypes/TestSiblingFieldPhi$Status;-><init>()V
    return-void
.end method
