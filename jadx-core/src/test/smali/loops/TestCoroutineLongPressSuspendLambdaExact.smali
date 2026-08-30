###### Class androidx.compose.foundation.gestures.DragGestureDetectorKt$awaitLongPressOrCancellation$2 (androidx.compose.foundation.gestures.DragGestureDetectorKt$awaitLongPressOrCancellation$2)
.class final Lloops/TestCoroutineLongPressSuspendLambdaExact;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;
.source "DragGestureDetector.kt"

# interfaces
.implements Lkotlin/jvm/functions/Function2;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Landroidx/compose/foundation/gestures/DragGestureDetectorKt;->awaitLongPressOrCancellation-rnUCldI(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = null
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
    value = "SMAP\nDragGestureDetector.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DragGestureDetector.kt\nandroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitLongPressOrCancellation$2\n+ 2 ListUtils.kt\nandroidx/compose/ui/util/ListUtilsKt\n*L\n1#1,1100:1\n88#2:1101\n35#2,5:1102\n89#2:1107\n103#2:1108\n35#2,5:1109\n104#2:1114\n103#2:1115\n35#2,5:1116\n104#2:1121\n118#2:1122\n35#2,5:1123\n119#2:1128\n118#2:1129\n35#2,5:1130\n119#2:1135\n*S KotlinDebug\n*F\n+ 1 DragGestureDetector.kt\nandroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitLongPressOrCancellation$2\n*L\n1027#1:1101\n1027#1:1102,5\n1027#1:1107\n1033#1:1108\n1033#1:1109,5\n1033#1:1114\n1049#1:1115\n1049#1:1116,5\n1049#1:1121\n1053#1:1122\n1053#1:1123,5\n1053#1:1128\n1063#1:1129\n1063#1:1130,5\n1063#1:1135\n*E\n"
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
        0x0,
        0x0
    }
    xi = 0x30
.end annotation

.annotation runtime Lkotlin/coroutines/jvm/internal/DebugMetadata;
    c = "androidx.compose.foundation.gestures.DragGestureDetectorKt$awaitLongPressOrCancellation$2"
    f = "DragGestureDetector.kt"
    i = {
        0x0,
        0x0,
        0x1,
        0x1,
        0x1
    }
    l = {
        0x402,
        0x418
    }
    m = "invokeSuspend"
    n = {
        "$this$withTimeout",
        "finished",
        "$this$withTimeout",
        "event",
        "finished"
    }
    s = {
        "L$0",
        "I$0",
        "L$0",
        "L$1",
        "I$0"
    }
    v = 0x1
.end annotation

.annotation build Lkotlin/jvm/internal/SourceDebugExtension;
    value = {
        "SMAP\nDragGestureDetector.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DragGestureDetector.kt\nandroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitLongPressOrCancellation$2\n+ 2 ListUtils.kt\nandroidx/compose/ui/util/ListUtilsKt\n*L\n1#1,1100:1\n88#2:1101\n35#2,5:1102\n89#2:1107\n103#2:1108\n35#2,5:1109\n104#2:1114\n103#2:1115\n35#2,5:1116\n104#2:1121\n118#2:1122\n35#2,5:1123\n119#2:1128\n118#2:1129\n35#2,5:1130\n119#2:1135\n*S KotlinDebug\n*F\n+ 1 DragGestureDetector.kt\nandroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitLongPressOrCancellation$2\n*L\n1027#1:1101\n1027#1:1102,5\n1027#1:1107\n1033#1:1108\n1033#1:1109,5\n1033#1:1114\n1049#1:1115\n1049#1:1116,5\n1049#1:1121\n1053#1:1122\n1053#1:1123,5\n1053#1:1128\n1063#1:1129\n1063#1:1130,5\n1063#1:1135\n*E\n"
    }
.end annotation


# instance fields
.field final synthetic $currentDown:Lkotlin/jvm/internal/Ref$ObjectRef;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Lkotlin/jvm/internal/Ref$ObjectRef<",
            "Landroidx/compose/ui/input/pointer/PointerInputChange;",
            ">;"
        }
    .end annotation
.end field

.field final synthetic $deepPress:Lkotlin/jvm/internal/Ref$BooleanRef;

.field final synthetic $longPress:Lkotlin/jvm/internal/Ref$ObjectRef;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Lkotlin/jvm/internal/Ref$ObjectRef<",
            "Landroidx/compose/ui/input/pointer/PointerInputChange;",
            ">;"
        }
    .end annotation
.end field

.field I$0:I

.field private synthetic L$0:Ljava/lang/Object;

.field L$1:Ljava/lang/Object;

.field label:I


# direct methods
.method public constructor <init>(Lkotlin/jvm/internal/Ref$BooleanRef;Lkotlin/jvm/internal/Ref$ObjectRef;Lkotlin/jvm/internal/Ref$ObjectRef;Lkotlin/coroutines/Continuation;)V
    .registers 5
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lkotlin/jvm/internal/Ref$BooleanRef;",
            "Lkotlin/jvm/internal/Ref$ObjectRef<",
            "Landroidx/compose/ui/input/pointer/PointerInputChange;",
            ">;",
            "Lkotlin/jvm/internal/Ref$ObjectRef<",
            "Landroidx/compose/ui/input/pointer/PointerInputChange;",
            ">;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lloops/TestCoroutineLongPressSuspendLambdaExact;",
            ">;)V"
        }
    .end annotation

    .line 1
    iput-object p1, p0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->$deepPress:Lkotlin/jvm/internal/Ref$BooleanRef;

    .line 2
    .line 3
    iput-object p2, p0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->$currentDown:Lkotlin/jvm/internal/Ref$ObjectRef;

    .line 4
    .line 5
    iput-object p3, p0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->$longPress:Lkotlin/jvm/internal/Ref$ObjectRef;

    .line 6
    .line 7
    const/4 p1, 0x2

    .line 8
    invoke-direct {p0, p1, p4}, Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V

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

    .line 1
    new-instance v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;

    .line 2
    .line 3
    iget-object v1, p0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->$deepPress:Lkotlin/jvm/internal/Ref$BooleanRef;

    .line 4
    .line 5
    iget-object v2, p0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->$currentDown:Lkotlin/jvm/internal/Ref$ObjectRef;

    .line 6
    .line 7
    iget-object v3, p0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->$longPress:Lkotlin/jvm/internal/Ref$ObjectRef;

    .line 8
    .line 9
    invoke-direct {v0, v1, v2, v3, p2}, Lloops/TestCoroutineLongPressSuspendLambdaExact;-><init>(Lkotlin/jvm/internal/Ref$BooleanRef;Lkotlin/jvm/internal/Ref$ObjectRef;Lkotlin/jvm/internal/Ref$ObjectRef;Lkotlin/coroutines/Continuation;)V

    .line 10
    .line 11
    .line 12
    iput-object p1, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->L$0:Ljava/lang/Object;

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

    .line 1
    invoke-virtual {p0, p1, p2}, Lloops/TestCoroutineLongPressSuspendLambdaExact;->create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;

    move-result-object p1

    check-cast p1, Lloops/TestCoroutineLongPressSuspendLambdaExact;

    sget-object p2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    invoke-virtual {p1, p2}, Lloops/TestCoroutineLongPressSuspendLambdaExact;->invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public bridge synthetic invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    .line 2
    check-cast p1, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    check-cast p2, Lkotlin/coroutines/Continuation;

    invoke-virtual {p0, p1, p2}, Lloops/TestCoroutineLongPressSuspendLambdaExact;->invoke(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 18

    .line 1
    move-object/from16 v0, p0

    .line 2
    .line 3
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    .line 4
    .line 5
    .line 6
    move-result-object v1

    .line 7
    iget v2, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->label:I

    .line 8
    .line 9
    const/4 v3, 0x2

    .line 10
    const/4 v4, 0x0

    .line 11
    const/4 v6, 0x1

    .line 12
    if-eqz v2, :cond_3b

    .line 13
    .line 14
    if-eq v2, v6, :cond_2f

    .line 15
    .line 16
    if-ne v2, v3, :cond_22

    .line 17
    .line 18
    iget v2, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->I$0:I

    .line 19
    .line 20
    iget-object v7, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->L$1:Ljava/lang/Object;

    .line 21
    .line 22
    check-cast v7, Landroidx/compose/ui/input/pointer/PointerEvent;

    .line 23
    .line 24
    iget-object v8, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->L$0:Ljava/lang/Object;

    .line 25
    .line 26
    check-cast v8, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    .line 27
    .line 28
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 29
    .line 30
    .line 31
    move-object/from16 v4, p1

    .line 32
    .line 33
    goto/16 :goto_c6

    .line 34
    .line 35
    :cond_22
    new-instance v1, Ljava/lang/IllegalStateException;

    .line 36
    .line 37
    const v2, 0x624cfed3

    invoke-static {v2}, Lfixtures/obfuscation/StringDecoder;->decode(I)Ljava/lang/String;

    move-result-object v2

    .line 38
    .line 39
    invoke-direct {v1, v2}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    .line 40
    .line 41
    .line 42
    throw v1

    .line 43
    :cond_2f
    iget v2, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->I$0:I

    .line 44
    .line 45
    iget-object v7, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->L$0:Ljava/lang/Object;

    .line 46
    .line 47
    check-cast v7, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    .line 48
    .line 49
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 50
    .line 51
    .line 52
    move-object/from16 v8, p1

    .line 53
    .line 54
    goto :goto_58

    .line 55
    :cond_3b
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 56
    .line 57
    .line 58
    iget-object v2, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->L$0:Ljava/lang/Object;

    .line 59
    .line 60
    check-cast v2, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    .line 61
    .line 62
    move-object v7, v2

    .line 63
    const/4 v2, 0x0

    .line 64
    :goto_44
    if-nez v2, :cond_165

    .line 65
    .line 66
    sget-object v8, Landroidx/compose/ui/input/pointer/PointerEventPass;->Main:Landroidx/compose/ui/input/pointer/PointerEventPass;

    .line 67
    .line 68
    iput-object v7, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->L$0:Ljava/lang/Object;

    .line 69
    .line 70
    iput-object v4, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->L$1:Ljava/lang/Object;

    .line 71
    .line 72
    iput v2, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->I$0:I

    .line 73
    .line 74
    iput v6, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->label:I

    .line 75
    .line 76
    invoke-interface {v7, v8, v0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 77
    .line 78
    .line 79
    move-result-object v8

    .line 80
    if-ne v8, v1, :cond_58

    .line 81
    .line 82
    goto/16 :goto_c2

    .line 83
    .line 84
    :cond_58
    :goto_58
    check-cast v8, Landroidx/compose/ui/input/pointer/PointerEvent;

    .line 85
    .line 86
    invoke-virtual {v8}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    .line 87
    .line 88
    .line 89
    move-result-object v9

    .line 90
    move-object v10, v9

    .line 91
    check-cast v10, Ljava/util/Collection;

    .line 92
    .line 93
    invoke-interface {v10}, Ljava/util/Collection;->size()I

    .line 94
    .line 95
    .line 96
    move-result v10

    .line 97
    const/4 v11, 0x0

    .line 98
    :goto_66
    if-ge v11, v10, :cond_78

    .line 99
    .line 100
    invoke-interface {v9, v11}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 101
    .line 102
    .line 103
    move-result-object v12

    .line 104
    check-cast v12, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 105
    .line 106
    invoke-static {v12}, Landroidx/compose/ui/input/pointer/PointerEventKt;->changedToUpIgnoreConsumed(Landroidx/compose/ui/input/pointer/PointerInputChange;)Z

    .line 107
    .line 108
    .line 109
    move-result v12

    .line 110
    if-nez v12, :cond_75

    .line 111
    .line 112
    goto :goto_79

    .line 113
    :cond_75
    add-int/lit8 v11, v11, 0x1

    .line 114
    .line 115
    goto :goto_66

    .line 116
    :cond_78
    move v2, v6

    .line 117
    :goto_79
    invoke-virtual {v8}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    .line 118
    .line 119
    .line 120
    move-result-object v9

    .line 121
    move-object v10, v9

    .line 122
    check-cast v10, Ljava/util/Collection;

    .line 123
    .line 124
    invoke-interface {v10}, Ljava/util/Collection;->size()I

    .line 125
    .line 126
    .line 127
    move-result v10

    .line 128
    const/4 v11, 0x0

    .line 129
    :goto_85
    if-ge v11, v10, :cond_a7

    .line 130
    .line 131
    invoke-interface {v9, v11}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 132
    .line 133
    .line 134
    move-result-object v12

    .line 135
    check-cast v12, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 136
    .line 137
    invoke-virtual {v12}, Landroidx/compose/ui/input/pointer/PointerInputChange;->isConsumed()Z

    .line 138
    .line 139
    .line 140
    move-result v13

    .line 141
    if-nez v13, :cond_a6

    .line 142
    .line 143
    invoke-interface {v7}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->getSize-YbymL2g()J

    .line 144
    .line 145
    .line 146
    move-result-wide v13

    .line 147
    invoke-interface {v7}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->getExtendedTouchPadding-NH-jbRc()J

    .line 148
    .line 149
    .line 150
    move-result-wide v4

    .line 151
    invoke-static {v12, v13, v14, v4, v5}, Landroidx/compose/ui/input/pointer/PointerEventKt;->isOutOfBounds-jwHxaWs(Landroidx/compose/ui/input/pointer/PointerInputChange;JJ)Z

    .line 152
    .line 153
    .line 154
    move-result v4

    .line 155
    if-eqz v4, :cond_a2

    .line 156
    .line 157
    goto :goto_a6

    .line 158
    :cond_a2
    add-int/lit8 v11, v11, 0x1

    .line 159
    .line 160
    const/4 v4, 0x0

    .line 161
    goto :goto_85

    .line 162
    :cond_a6
    :goto_a6
    move v2, v6

    .line 163
    :cond_a7
    invoke-static {v8}, Landroidx/compose/foundation/gestures/TapGestureDetector_androidKt;->isDeepPress(Landroidx/compose/ui/input/pointer/PointerEvent;)Z

    .line 164
    .line 165
    .line 166
    move-result v4

    .line 167
    if-eqz v4, :cond_b2

    .line 168
    .line 169
    iget-object v2, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->$deepPress:Lkotlin/jvm/internal/Ref$BooleanRef;

    .line 170
    .line 171
    iput-boolean v6, v2, Lkotlin/jvm/internal/Ref$BooleanRef;->element:Z

    .line 172
    .line 173
    move v2, v6

    .line 174
    :cond_b2
    sget-object v4, Landroidx/compose/ui/input/pointer/PointerEventPass;->Final:Landroidx/compose/ui/input/pointer/PointerEventPass;

    .line 175
    .line 176
    iput-object v7, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->L$0:Ljava/lang/Object;

    .line 177
    .line 178
    iput-object v8, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->L$1:Ljava/lang/Object;

    .line 179
    .line 180
    iput v2, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->I$0:I

    .line 181
    .line 182
    iput v3, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->label:I

    .line 183
    .line 184
    invoke-interface {v7, v4, v0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 185
    .line 186
    .line 187
    move-result-object v4

    .line 188
    if-ne v4, v1, :cond_c3

    .line 189
    .line 190
    :goto_c2
    return-object v1

    .line 191
    :cond_c3
    move-object v15, v8

    .line 192
    move-object v8, v7

    .line 193
    move-object v7, v15

    .line 194
    :goto_c6
    check-cast v4, Landroidx/compose/ui/input/pointer/PointerEvent;

    .line 195
    .line 196
    invoke-virtual {v4}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    .line 197
    .line 198
    .line 199
    move-result-object v4

    .line 200
    move-object v5, v4

    .line 201
    check-cast v5, Ljava/util/Collection;

    .line 202
    .line 203
    invoke-interface {v5}, Ljava/util/Collection;->size()I

    .line 204
    .line 205
    .line 206
    move-result v5

    .line 207
    const/4 v9, 0x0

    .line 208
    :goto_d4
    if-ge v9, v5, :cond_e7

    .line 209
    .line 210
    invoke-interface {v4, v9}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 211
    .line 212
    .line 213
    move-result-object v10

    .line 214
    check-cast v10, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 215
    .line 216
    invoke-virtual {v10}, Landroidx/compose/ui/input/pointer/PointerInputChange;->isConsumed()Z

    .line 217
    .line 218
    .line 219
    move-result v10

    .line 220
    if-eqz v10, :cond_e4

    .line 221
    .line 222
    move v2, v6

    .line 223
    goto :goto_e7

    .line 224
    :cond_e4
    add-int/lit8 v9, v9, 0x1

    .line 225
    .line 226
    goto :goto_d4

    .line 227
    :cond_e7
    :goto_e7
    iget-object v4, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->$currentDown:Lkotlin/jvm/internal/Ref$ObjectRef;

    .line 228
    .line 229
    iget-object v4, v4, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;

    .line 230
    .line 231
    check-cast v4, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 232
    .line 233
    invoke-virtual {v4}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getId-J3iCeTQ()J

    .line 234
    .line 235
    .line 236
    move-result-wide v4

    .line 237
    invoke-static {v7, v4, v5}, Landroidx/compose/foundation/gestures/DragGestureDetectorKt;->access$isPointerUp-DmW0f2w(Landroidx/compose/ui/input/pointer/PointerEvent;J)Z

    .line 238
    .line 239
    .line 240
    move-result v4

    .line 241
    if-eqz v4, :cond_129

    .line 242
    .line 243
    invoke-virtual {v7}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    .line 244
    .line 245
    .line 246
    move-result-object v4

    .line 247
    move-object v5, v4

    .line 248
    check-cast v5, Ljava/util/Collection;

    .line 249
    .line 250
    invoke-interface {v5}, Ljava/util/Collection;->size()I

    .line 251
    .line 252
    .line 253
    move-result v5

    .line 254
    const/4 v7, 0x0

    .line 255
    :goto_103
    if-ge v7, v5, :cond_116

    .line 256
    .line 257
    invoke-interface {v4, v7}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 258
    .line 259
    .line 260
    move-result-object v9

    .line 261
    move-object v10, v9

    .line 262
    check-cast v10, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 263
    .line 264
    invoke-virtual {v10}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getPressed()Z

    .line 265
    .line 266
    .line 267
    move-result v10

    .line 268
    if-eqz v10, :cond_113

    .line 269
    .line 270
    goto :goto_117

    .line 271
    :cond_113
    add-int/lit8 v7, v7, 0x1

    .line 272
    .line 273
    goto :goto_103

    .line 274
    :cond_116
    const/4 v9, 0x0

    .line 275
    :goto_117
    check-cast v9, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 276
    .line 277
    if-eqz v9, :cond_124

    .line 278
    .line 279
    iget-object v4, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->$currentDown:Lkotlin/jvm/internal/Ref$ObjectRef;

    .line 280
    .line 281
    iput-object v9, v4, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;

    .line 282
    .line 283
    iget-object v4, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->$longPress:Lkotlin/jvm/internal/Ref$ObjectRef;

    .line 284
    .line 285
    iput-object v9, v4, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;

    .line 286
    .line 287
    goto :goto_160

    .line 288
    :cond_124
    move v2, v6

    .line 289
    move-object v7, v8

    .line 290
    const/4 v4, 0x0

    .line 291
    goto/16 :goto_44

    .line 292
    .line 293
    :cond_129
    iget-object v4, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->$longPress:Lkotlin/jvm/internal/Ref$ObjectRef;

    .line 294
    .line 295
    invoke-virtual {v7}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    .line 296
    .line 297
    .line 298
    move-result-object v5

    .line 299
    iget-object v7, v0, Lloops/TestCoroutineLongPressSuspendLambdaExact;->$currentDown:Lkotlin/jvm/internal/Ref$ObjectRef;

    .line 300
    .line 301
    move-object v9, v5

    .line 302
    check-cast v9, Ljava/util/Collection;

    .line 303
    .line 304
    invoke-interface {v9}, Ljava/util/Collection;->size()I

    .line 305
    .line 306
    .line 307
    move-result v9

    .line 308
    const/4 v10, 0x0

    .line 309
    :goto_139
    if-ge v10, v9, :cond_15d

    .line 310
    .line 311
    invoke-interface {v5, v10}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 312
    .line 313
    .line 314
    move-result-object v11

    .line 315
    move-object v12, v11

    .line 316
    check-cast v12, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 317
    .line 318
    invoke-virtual {v12}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getId-J3iCeTQ()J

    .line 319
    .line 320
    .line 321
    move-result-wide v12

    .line 322
    iget-object v14, v7, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;

    .line 323
    .line 324
    check-cast v14, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 325
    .line 326
    move-object/from16 p1, v7

    .line 327
    .line 328
    invoke-virtual {v14}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getId-J3iCeTQ()J

    .line 329
    .line 330
    .line 331
    move-result-wide v6

    .line 332
    invoke-static {v12, v13, v6, v7}, Landroidx/compose/ui/input/pointer/PointerId;->equals-impl0(JJ)Z

    .line 333
    .line 334
    .line 335
    move-result v6

    .line 336
    if-eqz v6, :cond_157

    .line 337
    .line 338
    goto :goto_15e

    .line 339
    :cond_157
    add-int/lit8 v10, v10, 0x1

    .line 340
    .line 341
    move-object/from16 v7, p1

    .line 342
    .line 343
    const/4 v6, 0x1

    .line 344
    goto :goto_139

    .line 345
    :cond_15d
    const/4 v11, 0x0

    .line 346
    :goto_15e
    iput-object v11, v4, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;

    .line 347
    .line 348
    :goto_160
    move-object v7, v8

    .line 349
    const/4 v4, 0x0

    .line 350
    const/4 v6, 0x1

    .line 351
    goto/16 :goto_44

    .line 352
    .line 353
    :cond_165
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    .line 354
    .line 355
    return-object v1
.end method
