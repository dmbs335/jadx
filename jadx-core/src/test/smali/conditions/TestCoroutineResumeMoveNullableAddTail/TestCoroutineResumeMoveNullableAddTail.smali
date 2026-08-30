.class public final Lconditions/TestCoroutineResumeMoveNullableAddTail;
.super Ljava/lang/Object;
.source "BarGraphResponse.kt"


# annotations
.annotation system Ldalvik/annotation/SourceDebugExtension;
    value = "SMAP\nBarGraphResponse.kt\nKotlin\n*S Kotlin\n*F\n+ 1 BarGraphResponse.kt\nfixtures/app/ktor/feed/resource/component/response/BarGraphResponseKt\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,39:1\n1617#2,9:40\n1869#2:49\n1870#2:51\n1626#2:52\n1#3:50\n*S KotlinDebug\n*F\n+ 1 BarGraphResponse.kt\nfixtures/app/ktor/feed/resource/component/response/BarGraphResponseKt\n*L\n33#1:40,9\n33#1:49\n33#1:51\n33#1:52\n33#1:50\n*E\n"
.end annotation

.annotation runtime Lkotlin/Metadata;
    d1 = {
        "\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0008\u0002\u001a\u0012\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u0086@\u00a2\u0006\u0002\u0010\u0003\u00a8\u0006\u0004"
    }
    d2 = {
        "mapToEntity",
        "Lfixtures/app/feed/domain/root/entity/component/IFeedEntity;",
        "Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;",
        "(Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;",
        "app_fixtureRelease"
    }
    k = 0x2
    mv = {
        0x2,
        0x2,
        0x0
    }
    xi = 0x30
.end annotation

.annotation build Lkotlin/jvm/internal/SourceDebugExtension;
    value = {
        "SMAP\nBarGraphResponse.kt\nKotlin\n*S Kotlin\n*F\n+ 1 BarGraphResponse.kt\nfixtures/app/ktor/feed/resource/component/response/BarGraphResponseKt\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,39:1\n1617#2,9:40\n1869#2:49\n1870#2:51\n1626#2:52\n1#3:50\n*S KotlinDebug\n*F\n+ 1 BarGraphResponse.kt\nfixtures/app/ktor/feed/resource/component/response/BarGraphResponseKt\n*L\n33#1:40,9\n33#1:49\n33#1:51\n33#1:52\n33#1:50\n*E\n"
    }
.end annotation


# direct methods
.method public static final mapToEntity(Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 21
    .param p0    # Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .param p1    # Lkotlin/coroutines/Continuation;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lfixtures/app/feed/domain/root/entity/component/IFeedEntity;",
            ">;)",
            "Ljava/lang/Object;"
        }
    .end annotation

    .annotation build Lorg/jetbrains/annotations/Nullable;
    .end annotation

    .line 1
    move-object/from16 v0, p1

    .line 2
    .line 3
    instance-of v1, v0, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;

    .line 4
    .line 5
    if-eqz v1, :cond_15

    .line 6
    .line 7
    move-object v1, v0

    .line 8
    check-cast v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;

    .line 9
    .line 10
    iget v2, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->label:I

    .line 11
    .line 12
    const/high16 v3, -0x80000000

    .line 13
    .line 14
    and-int v4, v2, v3

    .line 15
    .line 16
    if-eqz v4, :cond_15

    .line 17
    .line 18
    sub-int/2addr v2, v3

    .line 19
    iput v2, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->label:I

    .line 20
    .line 21
    goto :goto_1a

    .line 22
    :cond_15
    new-instance v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;

    .line 23
    .line 24
    invoke-direct {v1, v0}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V

    .line 25
    .line 26
    .line 27
    :goto_1a
    iget-object v0, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->result:Ljava/lang/Object;

    .line 28
    .line 29
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    .line 30
    .line 31
    .line 32
    move-result-object v2

    .line 33
    iget v3, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->label:I

    .line 34
    .line 35
    const/4 v4, 0x1

    .line 36
    if-eqz v3, :cond_83

    .line 37
    .line 38
    if-ne v3, v4, :cond_76

    .line 39
    .line 40
    iget v3, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->I$3:I

    .line 41
    .line 42
    iget v6, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->I$2:I

    .line 43
    .line 44
    iget v7, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->I$1:I

    .line 45
    .line 46
    iget v8, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->I$0:I

    .line 47
    .line 48
    iget-object v9, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$13:Ljava/lang/Object;

    .line 49
    .line 50
    check-cast v9, Lfixtures/app/ktor/feed/resource/component/response/common/GraphItemResponse;

    .line 51
    .line 52
    iget-object v9, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$10:Ljava/lang/Object;

    .line 53
    .line 54
    check-cast v9, Ljava/util/Iterator;

    .line 55
    .line 56
    iget-object v10, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$9:Ljava/lang/Object;

    .line 57
    .line 58
    check-cast v10, Ljava/lang/Iterable;

    .line 59
    .line 60
    iget-object v11, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$8:Ljava/lang/Object;

    .line 61
    .line 62
    check-cast v11, Ljava/util/Collection;

    .line 63
    .line 64
    iget-object v12, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$7:Ljava/lang/Object;

    .line 65
    .line 66
    check-cast v12, Ljava/lang/Iterable;

    .line 67
    .line 68
    iget-object v13, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$6:Ljava/lang/Object;

    .line 69
    .line 70
    check-cast v13, Ljava/lang/String;

    .line 71
    .line 72
    iget-object v14, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$5:Ljava/lang/Object;

    .line 73
    .line 74
    check-cast v14, Ljava/lang/String;

    .line 75
    .line 76
    iget-object v15, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$4:Ljava/lang/Object;

    .line 77
    .line 78
    check-cast v15, Lfixtures/app/feed/domain/root/entity/component/common/SectionTitle;

    .line 79
    .line 80
    iget-object v4, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$3:Ljava/lang/Object;

    .line 81
    .line 82
    check-cast v4, Lfixtures/app/feed/domain/root/entity/component/common/GraphId;

    .line 83
    .line 84
    iget-object v5, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$2:Ljava/lang/Object;

    .line 85
    .line 86
    check-cast v5, Ljava/lang/Iterable;

    .line 87
    .line 88
    move-object/from16 v16, v0

    .line 89
    .line 90
    iget-object v0, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$1:Ljava/lang/Object;

    .line 91
    .line 92
    check-cast v0, Lfixtures/app/feed/domain/root/entity/component/common/GraphId;

    .line 93
    .line 94
    move-object/from16 p0, v0

    .line 95
    .line 96
    iget-object v0, v1, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$0:Ljava/lang/Object;

    .line 97
    .line 98
    check-cast v0, Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;

    .line 99
    .line 100
    invoke-static/range {v16 .. v16}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 101
    .line 102
    .line 103
    move-object/from16 v18, v0

    .line 104
    .line 105
    move-object/from16 v17, v14

    .line 106
    .line 107
    move-object v14, v13

    .line 108
    move-object v13, v12

    .line 109
    move-object v12, v11

    .line 110
    move-object v11, v10

    .line 111
    move-object v10, v9

    .line 112
    move-object v9, v4

    .line 113
    move v4, v3

    .line 114
    move-object v3, v1

    .line 115
    :goto_72
    move-object/from16 v1, p0

    .line 116
    .line 117
    goto/16 :goto_131

    .line 118
    .line 119
    :cond_76
    new-instance v0, Ljava/lang/IllegalStateException;

    .line 120
    .line 121
    const v1, 0x624cfed3

    invoke-static {v1}, Lfixtures/obfuscation/StringDecoder;->m12827(I)Ljava/lang/String;

    move-result-object v1

    .line 122
    .line 123
    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    .line 124
    .line 125
    .line 126
    throw v0

    .line 127
    :cond_83
    move-object/from16 v16, v0

    .line 128
    .line 129
    invoke-static/range {v16 .. v16}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 130
    .line 131
    .line 132
    invoke-virtual/range {p0 .. p0}, Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;->getGraphId()Ljava/lang/String;

    .line 133
    .line 134
    .line 135
    move-result-object v0

    .line 136
    invoke-static {v0}, Lfixtures/app/ktor/feed/resource/component/response/common/GraphItemResponseKt;->mapGraphIdToEnum(Ljava/lang/String;)Lfixtures/app/feed/domain/root/entity/component/common/GraphId;

    .line 137
    .line 138
    .line 139
    move-result-object v0

    .line 140
    if-eqz v0, :cond_166

    .line 141
    .line 142
    invoke-virtual/range {p0 .. p0}, Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;->getId()Ljava/lang/String;

    .line 143
    .line 144
    .line 145
    move-result-object v3

    .line 146
    invoke-virtual/range {p0 .. p0}, Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;->getAnalyticsId()Ljava/lang/String;

    .line 147
    .line 148
    .line 149
    move-result-object v4

    .line 150
    invoke-virtual/range {p0 .. p0}, Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;->getSectionTitle()Lfixtures/app/ktor/feed/resource/component/response/common/SectionTitleResponse;

    .line 151
    .line 152
    .line 153
    move-result-object v5

    .line 154
    if-eqz v5, :cond_a5

    .line 155
    .line 156
    invoke-static {v5}, Lfixtures/app/ktor/feed/resource/component/response/common/SectionTitleResponseKt;->mapToEntity(Lfixtures/app/ktor/feed/resource/component/response/common/SectionTitleResponse;)Lfixtures/app/feed/domain/root/entity/component/common/SectionTitle;

    .line 157
    .line 158
    .line 159
    move-result-object v5

    .line 160
    goto :goto_a6

    .line 161
    :cond_a5
    const/4 v5, 0x0

    .line 162
    :goto_a6
    invoke-virtual/range {p0 .. p0}, Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;->getGraphs()Ljava/util/List;

    .line 163
    .line 164
    .line 165
    move-result-object v6

    .line 166
    check-cast v6, Ljava/lang/Iterable;

    .line 167
    .line 168
    new-instance v7, Ljava/util/ArrayList;

    .line 169
    .line 170
    invoke-direct {v7}, Ljava/util/ArrayList;-><init>()V

    .line 171
    .line 172
    .line 173
    invoke-interface {v6}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;

    .line 174
    .line 175
    .line 176
    move-result-object v8

    .line 177
    move-object v9, v0

    .line 178
    move-object v14, v3

    .line 179
    move-object v15, v4

    .line 180
    move-object v11, v6

    .line 181
    move-object v13, v11

    .line 182
    move-object v12, v7

    .line 183
    move-object v10, v8

    .line 184
    const/4 v4, 0x0

    .line 185
    const/4 v7, 0x0

    .line 186
    const/4 v8, 0x0

    .line 187
    move-object/from16 v0, p0

    .line 188
    .line 189
    move-object/from16 p0, v9

    .line 190
    .line 191
    move-object v3, v1

    .line 192
    move-object v1, v5

    .line 193
    move-object v5, v13

    .line 194
    const/4 v6, 0x0

    .line 195
    :goto_c7
    invoke-interface {v10}, Ljava/util/Iterator;->hasNext()Z

    .line 196
    .line 197
    .line 198
    move-result v16

    .line 199
    if-eqz v16, :cond_142

    .line 200
    .line 201
    invoke-interface {v10}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 202
    .line 203
    .line 204
    move-result-object v16

    .line 205
    move-object/from16 v17, v5

    .line 206
    .line 207
    move-object/from16 v5, v16

    .line 208
    .line 209
    check-cast v5, Lfixtures/app/ktor/feed/resource/component/response/common/GraphItemResponse;

    .line 210
    .line 211
    iput-object v0, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$0:Ljava/lang/Object;

    .line 212
    .line 213
    move-object/from16 v18, v0

    .line 214
    .line 215
    invoke-static/range {p0 .. p0}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    .line 216
    .line 217
    .line 218
    move-result-object v0

    .line 219
    iput-object v0, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$1:Ljava/lang/Object;

    .line 220
    .line 221
    invoke-static/range {v17 .. v17}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    .line 222
    .line 223
    .line 224
    move-result-object v0

    .line 225
    iput-object v0, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$2:Ljava/lang/Object;

    .line 226
    .line 227
    iput-object v9, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$3:Ljava/lang/Object;

    .line 228
    .line 229
    iput-object v1, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$4:Ljava/lang/Object;

    .line 230
    .line 231
    iput-object v15, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$5:Ljava/lang/Object;

    .line 232
    .line 233
    iput-object v14, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$6:Ljava/lang/Object;

    .line 234
    .line 235
    invoke-static {v13}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    .line 236
    .line 237
    .line 238
    move-result-object v0

    .line 239
    iput-object v0, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$7:Ljava/lang/Object;

    .line 240
    .line 241
    iput-object v12, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$8:Ljava/lang/Object;

    .line 242
    .line 243
    invoke-static {v11}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    .line 244
    .line 245
    .line 246
    move-result-object v0

    .line 247
    iput-object v0, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$9:Ljava/lang/Object;

    .line 248
    .line 249
    iput-object v10, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$10:Ljava/lang/Object;

    .line 250
    .line 251
    invoke-static/range {v16 .. v16}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    .line 252
    .line 253
    .line 254
    move-result-object v0

    .line 255
    iput-object v0, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$11:Ljava/lang/Object;

    .line 256
    .line 257
    invoke-static/range {v16 .. v16}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    .line 258
    .line 259
    .line 260
    move-result-object v0

    .line 261
    iput-object v0, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$12:Ljava/lang/Object;

    .line 262
    .line 263
    invoke-static {v5}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    .line 264
    .line 265
    .line 266
    move-result-object v0

    .line 267
    iput-object v0, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->L$13:Ljava/lang/Object;

    .line 268
    .line 269
    iput v8, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->I$0:I

    .line 270
    .line 271
    iput v7, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->I$1:I

    .line 272
    .line 273
    iput v6, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->I$2:I

    .line 274
    .line 275
    iput v4, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->I$3:I

    .line 276
    .line 277
    const/4 v0, 0x0

    .line 278
    iput v0, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->I$4:I

    .line 279
    .line 280
    iput v0, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->I$5:I

    .line 281
    .line 282
    const/4 v0, 0x1

    .line 283
    iput v0, v3, Lconditions/TestCoroutineResumeMoveNullableAddTail$mapToEntity$1;->label:I

    .line 284
    .line 285
    invoke-static {v5, v3}, Lfixtures/app/ktor/feed/resource/component/response/common/GraphItemResponseKt;->mapToEntity(Lfixtures/app/ktor/feed/resource/component/response/common/GraphItemResponse;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 286
    .line 287
    .line 288
    move-result-object v5

    .line 289
    if-ne v5, v2, :cond_128

    .line 290
    .line 291
    return-object v2

    .line 292
    :cond_128
    move-object/from16 v16, v5

    .line 293
    .line 294
    move-object/from16 v5, v17

    .line 295
    .line 296
    move-object/from16 v17, v15

    .line 297
    .line 298
    move-object v15, v1

    .line 299
    goto/16 :goto_72

    .line 300
    .line 301
    :goto_131
    move-object/from16 v0, v16

    .line 302
    .line 303
    check-cast v0, Lfixtures/app/feed/domain/root/entity/component/common/GraphItem;

    .line 304
    .line 305
    if-eqz v0, :cond_13a

    .line 306
    .line 307
    invoke-interface {v12, v0}, Ljava/util/Collection;->add(Ljava/lang/Object;)Z

    .line 308
    .line 309
    .line 310
    :cond_13a
    move-object/from16 p0, v1

    .line 311
    .line 312
    move-object v1, v15

    .line 313
    move-object/from16 v15, v17

    .line 314
    .line 315
    move-object/from16 v0, v18

    .line 316
    .line 317
    goto :goto_c7

    .line 318
    :cond_142
    move-object/from16 v18, v0

    .line 319
    .line 320
    move-object v10, v12

    .line 321
    check-cast v10, Ljava/util/List;

    .line 322
    .line 323
    invoke-virtual/range {v18 .. v18}, Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;->getBottomButtons()Ljava/util/List;

    .line 324
    .line 325
    .line 326
    move-result-object v0

    .line 327
    invoke-static {v0}, Lfixtures/app/ktor/feed/resource/component/response/common/BottomButtonResponseKt;->mapToEntities(Ljava/util/List;)Ljava/util/List;

    .line 328
    .line 329
    .line 330
    move-result-object v11

    .line 331
    invoke-virtual/range {v18 .. v18}, Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;->getShowableBarCount()I

    .line 332
    .line 333
    .line 334
    move-result v0

    .line 335
    if-gtz v0, :cond_158

    .line 336
    .line 337
    const/4 v0, 0x6

    .line 338
    :goto_156
    move v12, v0

    .line 339
    goto :goto_15d

    .line 340
    :cond_158
    invoke-virtual/range {v18 .. v18}, Lfixtures/app/ktor/feed/resource/component/response/BarGraphResponse;->getShowableBarCount()I

    .line 341
    .line 342
    .line 343
    move-result v0

    .line 344
    goto :goto_156

    .line 345
    :goto_15d
    new-instance v5, Lfixtures/app/feed/domain/root/entity/component/BarGraphEntity;

    .line 346
    .line 347
    move-object v8, v1

    .line 348
    move-object v6, v14

    .line 349
    move-object v7, v15

    .line 350
    invoke-direct/range {v5 .. v12}, Lfixtures/app/feed/domain/root/entity/component/BarGraphEntity;-><init>(Ljava/lang/String;Ljava/lang/String;Lfixtures/app/feed/domain/root/entity/component/common/SectionTitle;Lfixtures/app/feed/domain/root/entity/component/common/GraphId;Ljava/util/List;Ljava/util/List;I)V

    .line 351
    .line 352
    .line 353
    return-object v5

    .line 354
    :cond_166
    sget-object v0, Lfixtures/app/feed/domain/root/entity/component/EmptyFeedEntity;->INSTANCE:Lfixtures/app/feed/domain/root/entity/component/EmptyFeedEntity;

    .line 355
    .line 356
    return-object v0
.end method
