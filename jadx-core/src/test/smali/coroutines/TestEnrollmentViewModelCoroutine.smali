###### Anonymized coroutine state-machine regression fixture
.class public final Lsample/app/enrollment/EnrollmentViewModel;
.super Lsample/app/viewmodels/BaseViewModel;
.source "SourceFile"


# instance fields
.field public final c:Landroidx/lifecycle/MutableLiveData;

.field public final d:Landroidx/lifecycle/MutableLiveData;

.field public final f:Landroidx/lifecycle/MutableLiveData;


# direct methods
.method public constructor <init>(Landroid/app/Application;Landroidx/lifecycle/SavedStateHandle;)V
    .registers 4

    const-string v0, "application"

    invoke-static {p1, v0}, Lkotlin/jvm/internal/l;->h(Ljava/lang/Object;Ljava/lang/String;)V

    const-string v0, "stateHandle"

    invoke-static {p2, v0}, Lkotlin/jvm/internal/l;->h(Ljava/lang/Object;Ljava/lang/String;)V

    invoke-direct {p0, p1, p2}, Lsample/app/viewmodels/BaseViewModel;-><init>(Landroid/app/Application;Landroidx/lifecycle/SavedStateHandle;)V

    new-instance p1, Landroidx/lifecycle/MutableLiveData;

    invoke-direct {p1}, Landroidx/lifecycle/MutableLiveData;-><init>()V

    iput-object p1, p0, Lsample/app/enrollment/EnrollmentViewModel;->c:Landroidx/lifecycle/MutableLiveData;

    new-instance p1, Landroidx/lifecycle/MutableLiveData;

    invoke-direct {p1}, Landroidx/lifecycle/MutableLiveData;-><init>()V

    iput-object p1, p0, Lsample/app/enrollment/EnrollmentViewModel;->d:Landroidx/lifecycle/MutableLiveData;

    new-instance p1, Landroidx/lifecycle/MutableLiveData;

    invoke-direct {p1}, Landroidx/lifecycle/MutableLiveData;-><init>()V

    iput-object p1, p0, Lsample/app/enrollment/EnrollmentViewModel;->f:Landroidx/lifecycle/MutableLiveData;

    return-void
.end method

.method public static final b(Lsample/app/enrollment/EnrollmentViewModel;Lkotlin/coroutines/jvm/internal/ContinuationImpl;)Ljava/lang/Object;
    .registers 25

    move-object/from16 v0, p0

    move-object/from16 v1, p1

    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    instance-of v2, v1, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;

    if-eqz v2, :cond_1a

    move-object v2, v1

    check-cast v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;

    iget v3, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->k:I

    const/high16 v4, -0x80000000

    and-int v5, v3, v4

    if-eqz v5, :cond_1a

    sub-int/2addr v3, v4

    iput v3, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->k:I

    goto :goto_1f

    :cond_1a
    new-instance v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;

    invoke-direct {v2, v0, v1}, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;-><init>(Lsample/app/enrollment/EnrollmentViewModel;Lkotlin/coroutines/c;)V

    :goto_1f
    iget-object v1, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->i:Ljava/lang/Object;

    sget-object v3, Lkotlin/coroutines/intrinsics/CoroutineSingletons;->c:Lkotlin/coroutines/intrinsics/CoroutineSingletons;

    iget v4, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->k:I

    const/4 v5, 0x5

    const/4 v6, 0x4

    const/4 v7, 0x3

    const/4 v8, 0x2

    const/4 v9, 0x1

    const/4 v10, 0x0

    if-eqz v4, :cond_7a

    if-eq v4, v9, :cond_74

    if-eq v4, v8, :cond_64

    if-eq v4, v7, :cond_55

    if-eq v4, v6, :cond_46

    if-ne v4, v5, :cond_3e

    iget-object v0, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->c:Lsample/app/enrollment/EnrollmentViewModel;

    invoke-static {v1}, Lkotlin/f;->b(Ljava/lang/Object;)V

    goto/16 :goto_16f

    :cond_3e
    new-instance v0, Ljava/lang/IllegalStateException;

    const-string v1, "call to \'resume\' before \'invoke\' with coroutine"

    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v0

    :cond_46
    iget-object v0, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->f:Ljava/lang/Object;

    check-cast v0, Lh0/e;

    iget-object v4, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->d:Landroid/os/Parcelable;

    check-cast v4, Lsample/network/api/entity/EnrollRequest;

    iget-object v6, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->c:Lsample/app/enrollment/EnrollmentViewModel;

    invoke-static {v1}, Lkotlin/f;->b(Ljava/lang/Object;)V

    goto/16 :goto_15d

    :cond_55
    iget-object v0, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->f:Ljava/lang/Object;

    check-cast v0, Lsample/network/api/entity/EnrollRequest;

    iget-object v4, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->d:Landroid/os/Parcelable;

    check-cast v4, Lsample/network/api/entity/EnrollRequest;

    iget-object v7, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->c:Lsample/app/enrollment/EnrollmentViewModel;

    invoke-static {v1}, Lkotlin/f;->b(Ljava/lang/Object;)V

    goto/16 :goto_12b

    :cond_64
    iget-object v0, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->g:Lsample/network/api/entity/EnrollRequest;

    iget-object v4, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->f:Ljava/lang/Object;

    check-cast v4, Lsample/network/api/entity/EnrollRequest;

    iget-object v8, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->d:Landroid/os/Parcelable;

    check-cast v8, Lsample/network/api/entity/AccountInfo;

    iget-object v9, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->c:Lsample/app/enrollment/EnrollmentViewModel;

    invoke-static {v1}, Lkotlin/f;->b(Ljava/lang/Object;)V

    goto :goto_c5

    :cond_74
    iget-object v0, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->c:Lsample/app/enrollment/EnrollmentViewModel;

    invoke-static {v1}, Lkotlin/f;->b(Ljava/lang/Object;)V

    goto :goto_8f

    :cond_7a
    invoke-static {v1}, Lkotlin/f;->b(Ljava/lang/Object;)V

    sget-object v1, Lsample/app/data/UserDataManager;->Companion:Lsample/app/data/UserDataManager$Companion;

    invoke-virtual {v1}, Lsample/app/data/UserDataManager$Companion;->getInstance()Lsample/app/data/UserDataManager;

    move-result-object v1

    iput-object v0, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->c:Lsample/app/enrollment/EnrollmentViewModel;

    iput v9, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->k:I

    invoke-virtual {v1, v2}, Lsample/app/data/UserDataManager;->getAccountInfo(Lkotlin/coroutines/c;)Ljava/lang/Object;

    move-result-object v1

    if-ne v1, v3, :cond_8f

    goto/16 :goto_16d

    :cond_8f
    :goto_8f
    check-cast v1, Lsample/network/api/entity/AccountInfo;

    new-instance v11, Lsample/network/api/entity/EnrollRequest;

    const/16 v21, 0x1ff

    const/16 v22, 0x0

    const/4 v12, 0x0

    const/4 v13, 0x0

    const/4 v14, 0x0

    const/4 v15, 0x0

    const/16 v16, 0x0

    const/16 v17, 0x0

    const/16 v18, 0x0

    const/16 v19, 0x0

    const/16 v20, 0x0

    invoke-direct/range {v11 .. v22}, Lsample/network/api/entity/EnrollRequest;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILkotlin/jvm/internal/i;)V

    sget-object v4, Lsample/app/data/DeviceDataManager;->Companion:Lsample/app/data/DeviceDataManager$Companion;

    invoke-virtual {v4}, Lsample/app/data/DeviceDataManager$Companion;->getInstance()Lsample/app/data/DeviceDataManager;

    move-result-object v4

    iput-object v0, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->c:Lsample/app/enrollment/EnrollmentViewModel;

    iput-object v1, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->d:Landroid/os/Parcelable;

    iput-object v11, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->f:Ljava/lang/Object;

    iput-object v11, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->g:Lsample/network/api/entity/EnrollRequest;

    iput v8, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->k:I

    invoke-virtual {v4, v2}, Lsample/app/data/DeviceDataManager;->getSelectedDevice(Lkotlin/coroutines/c;)Ljava/lang/Object;

    move-result-object v4

    if-ne v4, v3, :cond_c0

    goto/16 :goto_16d

    :cond_c0
    move-object v9, v0

    move-object v8, v1

    move-object v1, v4

    move-object v0, v11

    move-object v4, v0

    :goto_c5
    check-cast v1, Lsample/network/api/entity/DeviceInfo;

    if-eqz v1, :cond_ce

    invoke-virtual {v1}, Lsample/network/api/entity/DeviceInfo;->getVin()Ljava/lang/String;

    move-result-object v1

    goto :goto_cf

    :cond_ce
    move-object v1, v10

    :goto_cf
    invoke-virtual {v0, v1}, Lsample/network/api/entity/EnrollRequest;->setCarVin(Ljava/lang/String;)V

    if-eqz v8, :cond_d9

    invoke-virtual {v8}, Lsample/network/api/entity/AccountInfo;->getFirstName()Ljava/lang/String;

    move-result-object v0

    goto :goto_da

    :cond_d9
    move-object v0, v10

    :goto_da
    invoke-virtual {v4, v0}, Lsample/network/api/entity/EnrollRequest;->setFirstName(Ljava/lang/String;)V

    if-eqz v8, :cond_e4

    invoke-virtual {v8}, Lsample/network/api/entity/AccountInfo;->getLastName()Ljava/lang/String;

    move-result-object v0

    goto :goto_e5

    :cond_e4
    move-object v0, v10

    :goto_e5
    invoke-virtual {v4, v0}, Lsample/network/api/entity/EnrollRequest;->setLastName(Ljava/lang/String;)V

    if-eqz v8, :cond_f6

    invoke-virtual {v8}, Lsample/network/api/entity/AccountInfo;->getPhones()Lsample/network/api/entity/Phones;

    move-result-object v0

    if-eqz v0, :cond_f6

    invoke-virtual {v0}, Lsample/network/api/entity/Phones;->getPrimary()Ljava/lang/String;

    move-result-object v0

    if-nez v0, :cond_104

    :cond_f6
    if-eqz v8, :cond_103

    invoke-virtual {v8}, Lsample/network/api/entity/AccountInfo;->getPhones()Lsample/network/api/entity/Phones;

    move-result-object v0

    if-eqz v0, :cond_103

    invoke-virtual {v0}, Lsample/network/api/entity/Phones;->getSecondary()Ljava/lang/String;

    move-result-object v0

    goto :goto_104

    :cond_103
    move-object v0, v10

    :cond_104
    :goto_104
    invoke-virtual {v4, v0}, Lsample/network/api/entity/EnrollRequest;->setPhoneNumber(Ljava/lang/String;)V

    if-eqz v8, :cond_10e

    invoke-virtual {v8}, Lsample/network/api/entity/AccountInfo;->getNotificationEmail()Ljava/lang/String;

    move-result-object v0

    goto :goto_10f

    :cond_10e
    move-object v0, v10

    :goto_10f
    invoke-virtual {v4, v0}, Lsample/network/api/entity/EnrollRequest;->setUserId(Ljava/lang/String;)V

    sget-object v0, Lsample/app/data/UserDataManager;->Companion:Lsample/app/data/UserDataManager$Companion;

    invoke-virtual {v0}, Lsample/app/data/UserDataManager$Companion;->getInstance()Lsample/app/data/UserDataManager;

    move-result-object v0

    iput-object v9, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->c:Lsample/app/enrollment/EnrollmentViewModel;

    iput-object v4, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->d:Landroid/os/Parcelable;

    iput-object v4, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->f:Ljava/lang/Object;

    iput-object v10, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->g:Lsample/network/api/entity/EnrollRequest;

    iput v7, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->k:I

    invoke-virtual {v0, v2}, Lsample/app/data/UserDataManager;->getUserName(Lkotlin/coroutines/c;)Ljava/lang/Object;

    move-result-object v1

    if-ne v1, v3, :cond_129

    goto :goto_16d

    :cond_129
    move-object v0, v4

    move-object v7, v9

    :goto_12b
    check-cast v1, Ljava/lang/String;

    invoke-virtual {v0, v1}, Lsample/network/api/entity/EnrollRequest;->setUserEmail(Ljava/lang/String;)V

    sget-object v0, Lsample/app/data/AppDataManager;->Companion:Lsample/app/data/AppDataManager$Companion;

    invoke-virtual {v0}, Lsample/app/data/AppDataManager$Companion;->getInstance()Lsample/app/data/AppDataManager;

    move-result-object v0

    invoke-virtual {v0}, Lsample/app/data/AppDataManager;->isDemo()Z

    move-result v0

    if-eqz v0, :cond_142

    new-instance v0, Lsample/network/repo/demo/DemoRepository;

    invoke-direct {v0}, Lsample/network/repo/demo/DemoRepository;-><init>()V

    goto :goto_147

    :cond_142
    new-instance v0, Lsample/network/repo/net/m;

    invoke-direct {v0}, Ljava/lang/Object;-><init>()V

    :goto_147
    sget-object v1, Lsample/app/data/UserDataManager;->Companion:Lsample/app/data/UserDataManager$Companion;

    invoke-virtual {v1}, Lsample/app/data/UserDataManager$Companion;->getInstance()Lsample/app/data/UserDataManager;

    move-result-object v1

    iput-object v7, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->c:Lsample/app/enrollment/EnrollmentViewModel;

    iput-object v4, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->d:Landroid/os/Parcelable;

    iput-object v0, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->f:Ljava/lang/Object;

    iput v6, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->k:I

    invoke-virtual {v1, v2}, Lsample/app/data/UserDataManager;->transToCommonHead(Lkotlin/coroutines/c;)Ljava/lang/Object;

    move-result-object v1

    if-ne v1, v3, :cond_15c

    goto :goto_16d

    :cond_15c
    move-object v6, v7

    :goto_15d
    check-cast v1, Lsample/network/api/entity/CommonHeadRequest;

    iput-object v6, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->c:Lsample/app/enrollment/EnrollmentViewModel;

    iput-object v10, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->d:Landroid/os/Parcelable;

    iput-object v10, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->f:Ljava/lang/Object;

    iput v5, v2, Lsample/app/enrollment/EnrollmentViewModel$getEnrollmentRequest$1;->k:I

    invoke-interface {v0, v1, v4, v2}, Lh0/e;->sendEnrollmentRequest(Lsample/network/api/entity/CommonHeadRequest;Lsample/network/api/entity/EnrollRequest;Lkotlin/coroutines/c;)Ljava/lang/Object;

    move-result-object v1

    if-ne v1, v3, :cond_16e

    :goto_16d
    return-object v3

    :cond_16e
    move-object v0, v6

    :goto_16f
    check-cast v1, Lsample/network/api/entity/CommonResponse;

    if-eqz v1, :cond_178

    invoke-virtual {v1}, Lsample/network/api/entity/CommonResponse;->getError()Lsample/network/api/entity/ErrorHeader;

    move-result-object v1

    goto :goto_179

    :cond_178
    move-object v1, v10

    :goto_179
    if-eqz v1, :cond_183

    iget-object v0, v0, Lsample/app/enrollment/EnrollmentViewModel;->f:Landroidx/lifecycle/MutableLiveData;

    sget-object v1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;

    invoke-virtual {v0, v1}, Landroidx/lifecycle/MutableLiveData;->setValue(Ljava/lang/Object;)V

    goto :goto_188

    :cond_183
    iget-object v0, v0, Lsample/app/enrollment/EnrollmentViewModel;->c:Landroidx/lifecycle/MutableLiveData;

    invoke-virtual {v0, v10}, Landroidx/lifecycle/MutableLiveData;->setValue(Ljava/lang/Object;)V

    :goto_188
    sget-object v0, Lkotlin/l;->a:Lkotlin/l;

    return-object v0
.end method


# virtual methods
.method public final c(Ljava/lang/Boolean;)V
    .registers 5

    invoke-static {p0}, Landroidx/lifecycle/ViewModelKt;->getViewModelScope(Landroidx/lifecycle/ViewModel;)Lkotlinx/coroutines/w;

    move-result-object v0

    new-instance v1, Lsample/app/enrollment/EnrollmentViewModel$checkEnrollment$1;

    const/4 v2, 0x0

    invoke-direct {v1, p1, p0, v2}, Lsample/app/enrollment/EnrollmentViewModel$checkEnrollment$1;-><init>(Ljava/lang/Boolean;Lsample/app/enrollment/EnrollmentViewModel;Lkotlin/coroutines/c;)V

    const/4 p0, 0x3

    invoke-static {v0, v2, v1, p0}, Lkotlinx/coroutines/x;->r(Lkotlinx/coroutines/w;Lkotlinx/coroutines/android/c;Lkotlin/jvm/functions/Function2;I)Lkotlinx/coroutines/g1;

    return-void
.end method

.method public final onCreate(Z)V
    .registers 2

    return-void
.end method
