.class final Lloops/TestCoroutineTapPointerEventNestedLoop;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;
.source "FeatureGateWrapper.kt"

# interfaces
.implements Lkotlin/jvm/functions/Function2;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lfixtures/sdk/ods/featuregate/FeatureGateWrapperKt$featureGate$1$1$1;->invoke(Landroidx/compose/ui/input/pointer/PointerInputScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = null
.end annotation

.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lfixtures/sdk/ods/featuregate/FeatureGateWrapperKt$featureGate$1$1$1$1$WhenMappings;
    }
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;",
        "Lkotlin/jvm/functions/Function2<",
        "Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;",
        "Lkotlin/coroutines/Continuation<",
        "-",
        "Lkotlin/Unit;",
        ">;",
        "Ljava/lang/Object;",
        ">;"
    }
.end annotation

.annotation system Ldalvik/annotation/SourceDebugExtension;
    value = "SMAP\nFeatureGateWrapper.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FeatureGateWrapper.kt\nfixtures/sdk/ods/featuregate/FeatureGateWrapperKt$featureGate$1$1$1$1\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,164:1\n295#2,2:165\n*S KotlinDebug\n*F\n+ 1 FeatureGateWrapper.kt\nfixtures/sdk/ods/featuregate/FeatureGateWrapperKt$featureGate$1$1$1$1\n*L\n131#1:165,2\n*E\n"
.end annotation

.annotation runtime Lkotlin/Metadata;
    d1 = {
        "\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"
    }
    d2 = {
        "<anonymous>",
        "",
        "Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;"
    }
    k = 0x3
    mv = {
        0x2,
        0x2,
        0x0
    }
    xi = 0x30
.end annotation

.annotation runtime Lkotlin/coroutines/jvm/internal/DebugMetadata;
    c = "fixtures.sdk.ods.featuregate.FeatureGateWrapperKt$featureGate$1$1$1$1"
    f = "FeatureGateWrapper.kt"
    i = {
        0x0,
        0x1,
        0x1,
        0x1,
        0x1,
        0x1
    }
    l = {
        0x82,
        0x88
    }
    m = "invokeSuspend"
    n = {
        "$this$awaitPointerEventScope",
        "$this$awaitPointerEventScope",
        "down",
        "downChange",
        "startPos",
        "isTap"
    }
    s = {
        "L$0",
        "L$0",
        "L$1",
        "L$2",
        "J$0",
        "I$0"
    }
    v = 0x1
.end annotation

.annotation build Lkotlin/jvm/internal/SourceDebugExtension;
    value = {
        "SMAP\nFeatureGateWrapper.kt\nKotlin\n*S Kotlin\n*F\n+ 1 FeatureGateWrapper.kt\nfixtures/sdk/ods/featuregate/FeatureGateWrapperKt$featureGate$1$1$1$1\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,164:1\n295#2,2:165\n*S KotlinDebug\n*F\n+ 1 FeatureGateWrapper.kt\nfixtures/sdk/ods/featuregate/FeatureGateWrapperKt$featureGate$1$1$1$1\n*L\n131#1:165,2\n*E\n"
    }
.end annotation


# instance fields
.field final synthetic $key:Lfixtures/sdk/core/featuregate/FeatureCode;

.field final synthetic $policy:Lfixtures/sdk/ods/featuregate/FeatureGatePolicy;

.field final synthetic $touchSlop:F

.field I$0:I

.field J$0:J

.field private synthetic L$0:Ljava/lang/Object;

.field L$1:Ljava/lang/Object;

.field L$2:Ljava/lang/Object;

.field label:I


# direct methods
.method public constructor <init>(FLfixtures/sdk/ods/featuregate/FeatureGatePolicy;Lfixtures/sdk/core/featuregate/FeatureCode;Lkotlin/coroutines/Continuation;)V
    .registers 5
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(F",
            "Lfixtures/sdk/ods/featuregate/FeatureGatePolicy;",
            "Lfixtures/sdk/core/featuregate/FeatureCode;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lloops/TestCoroutineTapPointerEventNestedLoop;",
            ">;)V"
        }
    .end annotation

    #@0
    .line 1
    iput p1, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->$touchSlop:F

    #@2
    .line 2
    .line 3
    iput-object p2, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->$policy:Lfixtures/sdk/ods/featuregate/FeatureGatePolicy;

    #@4
    .line 4
    .line 5
    iput-object p3, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->$key:Lfixtures/sdk/core/featuregate/FeatureCode;

    #@6
    .line 6
    .line 7
    const/4 p1, 0x2

    #@7
    .line 8
    invoke-direct {p0, p1, p4}, Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V

    #@a
    .line 9
    .line 10
    .line 11
    return-void
.end method
# virtual methods
.method public final create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;
    .registers 7
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/Object;",
            "Lkotlin/coroutines/Continuation<",
            "*>;)",
            "Lkotlin/coroutines/Continuation<",
            "Lkotlin/Unit;",
            ">;"
        }
    .end annotation

    #@0
    .line 1
    new-instance v0, Lloops/TestCoroutineTapPointerEventNestedLoop;

    #@2
    .line 2
    .line 3
    iget v1, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->$touchSlop:F

    #@4
    .line 4
    .line 5
    iget-object v2, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->$policy:Lfixtures/sdk/ods/featuregate/FeatureGatePolicy;

    #@6
    .line 6
    .line 7
    iget-object v3, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->$key:Lfixtures/sdk/core/featuregate/FeatureCode;

    #@8
    .line 8
    .line 9
    invoke-direct {v0, v1, v2, v3, p2}, Lloops/TestCoroutineTapPointerEventNestedLoop;-><init>(FLfixtures/sdk/ods/featuregate/FeatureGatePolicy;Lfixtures/sdk/core/featuregate/FeatureCode;Lkotlin/coroutines/Continuation;)V

    #@b
    .line 10
    .line 11
    .line 12
    iput-object p1, v0, Lloops/TestCoroutineTapPointerEventNestedLoop;->L$0:Ljava/lang/Object;

    #@d
    .line 13
    .line 14
    return-object v0
.end method

.method public final invoke(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lkotlin/Unit;",
            ">;)",
            "Ljava/lang/Object;"
        }
    .end annotation

    #@0
    .line 1
    invoke-virtual {p0, p1, p2}, Lloops/TestCoroutineTapPointerEventNestedLoop;->create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;

    #@3
    move-result-object p1

    #@4
    check-cast p1, Lloops/TestCoroutineTapPointerEventNestedLoop;

    #@6
    sget-object p2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    #@8
    invoke-virtual {p1, p2}, Lloops/TestCoroutineTapPointerEventNestedLoop;->invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;

    #@b
    move-result-object p1

    #@c
    return-object p1
.end method

.method public bridge synthetic invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    #@0
    .line 2
    check-cast p1, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    #@2
    check-cast p2, Lkotlin/coroutines/Continuation;

    #@4
    invoke-virtual {p0, p1, p2}, Lloops/TestCoroutineTapPointerEventNestedLoop;->invoke(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    #@7
    move-result-object p1

    #@8
    return-object p1
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 16

    #@0
    .line 1
    iget-object v0, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->L$0:Ljava/lang/Object;

    #@2
    .line 2
    .line 3
    check-cast v0, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    #@4
    .line 4
    .line 5
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    #@7
    .line 6
    .line 7
    .line 8
    move-result-object v1

    #@8
    .line 9
    iget v2, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->label:I

    #@a
    .line 10
    .line 11
    const/4 v3, 0x0

    #@b
    .line 12
    const/4 v4, 0x2

    #@c
    .line 13
    const/4 v5, 0x1

    #@d
    .line 14
    if-eqz v2, :cond_35

    #@f
    .line 15
    .line 16
    if-eq v2, v5, :cond_31

    #@11
    .line 17
    .line 18
    if-ne v2, v4, :cond_24

    #@13
    .line 19
    .line 20
    iget v2, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->I$0:I

    #@15
    .line 21
    .line 22
    iget-wide v6, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->J$0:J

    #@17
    .line 23
    .line 24
    iget-object v8, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->L$2:Ljava/lang/Object;

    #@19
    .line 25
    .line 26
    check-cast v8, Landroidx/compose/ui/input/pointer/PointerInputChange;

    #@1b
    .line 27
    .line 28
    iget-object v9, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->L$1:Ljava/lang/Object;

    #@1d
    .line 29
    .line 30
    check-cast v9, Landroidx/compose/ui/input/pointer/PointerEvent;

    #@1f
    .line 31
    .line 32
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    #@22
    .line 33
    .line 34
    .line 35
    goto/16 :goto_95

    #@24
    .line 36
    .line 37
    :cond_24
    new-instance p1, Ljava/lang/IllegalStateException;

    #@26
    .line 38
    .line 39
    const v0, -0x5a56cf8d

    #@29
    invoke-static {v0}, Lfixtures/obfuscation/StringDecoder;->̎ɏˑƌ(I)Ljava/lang/String;

    #@2c
    move-result-object v0

    #@2d
    .line 40
    .line 41
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    #@30
    .line 42
    .line 43
    .line 44
    throw p1

    #@31
    .line 45
    :cond_31
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    #@34
    .line 46
    .line 47
    .line 48
    goto :goto_49

    #@35
    .line 49
    :cond_35
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    #@38
    .line 50
    .line 51
    .line 52
    :cond_38
    :goto_38
    sget-object p1, Landroidx/compose/ui/input/pointer/PointerEventPass;->Initial:Landroidx/compose/ui/input/pointer/PointerEventPass;

    #@3a
    .line 53
    .line 54
    iput-object v0, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->L$0:Ljava/lang/Object;

    #@3c
    .line 55
    .line 56
    iput-object v3, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->L$1:Ljava/lang/Object;

    #@3e
    .line 57
    .line 58
    iput-object v3, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->L$2:Ljava/lang/Object;

    #@40
    .line 59
    .line 60
    iput v5, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->label:I

    #@42
    .line 61
    .line 62
    invoke-interface {v0, p1, p0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    #@45
    .line 63
    .line 64
    .line 65
    move-result-object p1

    #@46
    .line 66
    if-ne p1, v1, :cond_49

    #@48
    .line 67
    .line 68
    goto :goto_94

    #@49
    .line 69
    :cond_49
    :goto_49
    check-cast p1, Landroidx/compose/ui/input/pointer/PointerEvent;

    #@4b
    .line 70
    .line 71
    invoke-virtual {p1}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    #@4e
    .line 72
    .line 73
    .line 74
    move-result-object v2

    #@4f
    .line 75
    check-cast v2, Ljava/lang/Iterable;

    #@51
    .line 76
    .line 77
    invoke-interface {v2}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;

    #@54
    .line 78
    .line 79
    .line 80
    move-result-object v2

    #@55
    .line 81
    :cond_55
    invoke-interface {v2}, Ljava/util/Iterator;->hasNext()Z

    #@58
    .line 82
    .line 83
    .line 84
    move-result v6

    #@59
    .line 85
    if-eqz v6, :cond_69

    #@5b
    .line 86
    .line 87
    invoke-interface {v2}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    #@5e
    .line 88
    .line 89
    .line 90
    move-result-object v6

    #@5f
    .line 91
    move-object v7, v6

    #@60
    .line 92
    check-cast v7, Landroidx/compose/ui/input/pointer/PointerInputChange;

    #@62
    .line 93
    .line 94
    invoke-static {v7}, Landroidx/compose/ui/input/pointer/PointerEventKt;->changedToDown(Landroidx/compose/ui/input/pointer/PointerInputChange;)Z

    #@65
    .line 95
    .line 96
    .line 97
    move-result v7

    #@66
    .line 98
    if-eqz v7, :cond_55

    #@68
    .line 99
    .line 100
    goto :goto_6a

    #@69
    .line 101
    :cond_69
    move-object v6, v3

    #@6a
    .line 102
    :goto_6a
    check-cast v6, Landroidx/compose/ui/input/pointer/PointerInputChange;

    #@6c
    .line 103
    .line 104
    if-nez v6, :cond_6f

    #@6e
    .line 105
    .line 106
    goto :goto_38

    #@6f
    .line 107
    :cond_6f
    invoke-virtual {v6}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getPosition-F1C5BW0()J

    #@72
    .line 108
    .line 109
    .line 110
    move-result-wide v7

    #@73
    .line 111
    move-wide v12, v7

    #@74
    .line 112
    move-object v8, v6

    #@75
    .line 113
    move-wide v6, v12

    #@76
    .line 114
    move-object v9, p1

    #@77
    .line 115
    move v2, v5

    #@78
    .line 116
    :cond_78
    sget-object p1, Landroidx/compose/ui/input/pointer/PointerEventPass;->Initial:Landroidx/compose/ui/input/pointer/PointerEventPass;

    #@7a
    .line 117
    .line 118
    iput-object v0, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->L$0:Ljava/lang/Object;

    #@7c
    .line 119
    .line 120
    invoke-static {v9}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    #@7f
    .line 121
    .line 122
    .line 123
    move-result-object v10

    #@80
    .line 124
    iput-object v10, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->L$1:Ljava/lang/Object;

    #@82
    .line 125
    .line 126
    invoke-static {v8}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    #@85
    .line 127
    .line 128
    .line 129
    move-result-object v10

    #@86
    .line 130
    iput-object v10, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->L$2:Ljava/lang/Object;

    #@88
    .line 131
    .line 132
    iput-wide v6, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->J$0:J

    #@8a
    .line 133
    .line 134
    iput v2, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->I$0:I

    #@8c
    .line 135
    .line 136
    iput v4, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->label:I

    #@8e
    .line 137
    .line 138
    invoke-interface {v0, p1, p0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    #@91
    .line 139
    .line 140
    .line 141
    move-result-object p1

    #@92
    .line 142
    if-ne p1, v1, :cond_95

    #@94
    .line 143
    .line 144
    :goto_94
    return-object v1

    #@95
    .line 145
    :cond_95
    :goto_95
    check-cast p1, Landroidx/compose/ui/input/pointer/PointerEvent;

    #@97
    .line 146
    .line 147
    invoke-virtual {p1}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    #@9a
    .line 148
    .line 149
    .line 150
    move-result-object p1

    #@9b
    .line 151
    invoke-static {p1}, Lkotlin/collections/q;->firstOrNull(Ljava/util/List;)Ljava/lang/Object;

    #@9e
    .line 152
    .line 153
    .line 154
    move-result-object p1

    #@9f
    .line 155
    check-cast p1, Landroidx/compose/ui/input/pointer/PointerInputChange;

    #@a1
    .line 156
    .line 157
    if-nez p1, :cond_a4

    #@a3
    .line 158
    .line 159
    goto :goto_38

    #@a4
    .line 160
    :cond_a4
    if-eqz v2, :cond_b9

    #@a6
    .line 161
    .line 162
    invoke-virtual {p1}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getPosition-F1C5BW0()J

    #@a9
    .line 163
    .line 164
    .line 165
    move-result-wide v10

    #@aa
    .line 166
    invoke-static {v10, v11, v6, v7}, Landroidx/compose/ui/geometry/Offset;->minus-MK-Hz9U(JJ)J

    #@ad
    .line 167
    .line 168
    .line 169
    move-result-wide v10

    #@ae
    .line 170
    invoke-static {v10, v11}, Landroidx/compose/ui/geometry/Offset;->getDistance-impl(J)F

    #@b1
    .line 171
    .line 172
    .line 173
    move-result v10

    #@b2
    .line 174
    iget v11, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->$touchSlop:F

    #@b4
    .line 175
    .line 176
    cmpl-float v10, v10, v11

    #@b6
    .line 177
    .line 178
    if-lez v10, :cond_b9

    #@b8
    .line 179
    .line 180
    const/4 v2, 0x0

    #@b9
    .line 181
    :cond_b9
    invoke-static {p1}, Landroidx/compose/ui/input/pointer/PointerEventKt;->changedToUp(Landroidx/compose/ui/input/pointer/PointerInputChange;)Z

    #@bc
    .line 182
    .line 183
    .line 184
    move-result p1

    #@bd
    .line 185
    if-eqz p1, :cond_78

    #@bf
    .line 186
    .line 187
    if-eqz v2, :cond_38

    #@c1
    .line 188
    .line 189
    iget-object p1, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->$policy:Lfixtures/sdk/ods/featuregate/FeatureGatePolicy;

    #@c3
    .line 190
    .line 191
    invoke-virtual {p1}, Lfixtures/sdk/ods/featuregate/FeatureGatePolicy;->getPolicies()Ljava/util/Map;

    #@c6
    .line 192
    .line 193
    .line 194
    move-result-object p1

    #@c7
    .line 195
    iget-object v2, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->$key:Lfixtures/sdk/core/featuregate/FeatureCode;

    #@c9
    .line 196
    .line 197
    invoke-interface {p1, v2}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    #@cc
    .line 198
    .line 199
    .line 200
    move-result-object p1

    #@cd
    .line 201
    const v2, -0x4b3a063a

    #@d0
    invoke-static {v2}, Lfixtures/obfuscation/StringDecoder;->˒ȏȏǏ(I)Ljava/lang/String;

    #@d3
    move-result-object v2

    #@d4
    .line 202
    .line 203
    invoke-static {p1, v2}, Lkotlin/jvm/internal/Intrinsics;->checkNotNull(Ljava/lang/Object;Ljava/lang/String;)V

    #@d7
    .line 204
    .line 205
    .line 206
    check-cast p1, Lfixtures/sdk/core/featuregate/FeatureGateStatusCode$Disabled;

    #@d9
    .line 207
    .line 208
    sget-object v2, Lfixtures/sdk/ods/featuregate/FeatureGateWrapperKt$featureGate$1$1$1$1$WhenMappings;->$EnumSwitchMapping$0:[I

    #@db
    .line 209
    .line 210
    invoke-virtual {p1}, Ljava/lang/Enum;->ordinal()I

    #@de
    .line 211
    .line 212
    .line 213
    move-result p1

    #@df
    .line 214
    aget p1, v2, p1

    #@e1
    .line 215
    .line 216
    if-eq p1, v5, :cond_fb

    #@e3
    .line 217
    .line 218
    if-eq p1, v4, :cond_f0

    #@e5
    .line 219
    .line 220
    const/4 v2, 0x3

    #@e6
    .line 221
    if-ne p1, v2, :cond_ea

    #@e8
    .line 222
    .line 223
    goto/16 :goto_38

    #@ea
    .line 224
    .line 225
    :cond_ea
    new-instance p1, Lkotlin/NoWhenBranchMatchedException;

    #@ec
    .line 226
    .line 227
    invoke-direct {p1}, Lkotlin/NoWhenBranchMatchedException;-><init>()V

    #@ef
    .line 228
    .line 229
    .line 230
    throw p1

    #@f0
    .line 231
    :cond_f0
    iget-object p1, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->$policy:Lfixtures/sdk/ods/featuregate/FeatureGatePolicy;

    #@f2
    .line 232
    .line 233
    invoke-virtual {p1}, Lfixtures/sdk/ods/featuregate/FeatureGatePolicy;->getAlertSVRNotification()Lkotlin/jvm/functions/Function0;

    #@f5
    .line 234
    .line 235
    .line 236
    move-result-object p1

    #@f6
    .line 237
    invoke-interface {p1}, Lkotlin/jvm/functions/Function0;->invoke()Ljava/lang/Object;

    #@f9
    .line 238
    .line 239
    .line 240
    goto/16 :goto_38

    #@fb
    .line 241
    .line 242
    :cond_fb
    iget-object p1, p0, Lloops/TestCoroutineTapPointerEventNestedLoop;->$policy:Lfixtures/sdk/ods/featuregate/FeatureGatePolicy;

    #@fd
    .line 243
    .line 244
    invoke-virtual {p1}, Lfixtures/sdk/ods/featuregate/FeatureGatePolicy;->getAlertNeedSubscription()Lkotlin/jvm/functions/Function0;

    #@100
    .line 245
    .line 246
    .line 247
    move-result-object p1

    #@101
    .line 248
    invoke-interface {p1}, Lkotlin/jvm/functions/Function0;->invoke()Ljava/lang/Object;

    #@104
    .line 249
    .line 250
    .line 251
    goto/16 :goto_38
.end method
