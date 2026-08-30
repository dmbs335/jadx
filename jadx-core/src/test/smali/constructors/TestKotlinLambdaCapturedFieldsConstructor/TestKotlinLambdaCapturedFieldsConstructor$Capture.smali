.class public final Lconstructors/TestKotlinLambdaCapturedFieldsConstructor$Capture;
.super Lkotlin/jvm/internal/Lambda;

.implements Lkotlin/jvm/functions/Function3;

.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lconstructors/TestKotlinLambdaCapturedFieldsConstructor;->make(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = null
.end annotation

.field final synthetic capture1:Ljava/lang/Object;
.field final synthetic capture2:Ljava/lang/Object;
.field final synthetic capture3:Ljava/lang/Object;
.field final synthetic capture4:Ljava/lang/Object;

.method public constructor <init>(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    .registers 5

    iput-object p1, p0, Lconstructors/TestKotlinLambdaCapturedFieldsConstructor$Capture;->capture1:Ljava/lang/Object;
    iput-object p2, p0, Lconstructors/TestKotlinLambdaCapturedFieldsConstructor$Capture;->capture2:Ljava/lang/Object;
    iput-object p3, p0, Lconstructors/TestKotlinLambdaCapturedFieldsConstructor$Capture;->capture3:Ljava/lang/Object;
    iput-object p4, p0, Lconstructors/TestKotlinLambdaCapturedFieldsConstructor$Capture;->capture4:Ljava/lang/Object;
    const/4 p1, 0x3
    invoke-direct {p0, p1}, Lkotlin/jvm/internal/Lambda;-><init>(I)V
    return-void
.end method

.method public invoke(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 4

    const/4 p1, 0x0
    return-object p1
.end method
