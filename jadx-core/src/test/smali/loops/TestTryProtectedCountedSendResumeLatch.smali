.class final Lloops/TestTryProtectedCountedSendResumeLatch;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;
.source "Nonce.kt"

# interfaces
.implements Lkotlin/jvm/functions/Function2;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lio/ktor/util/NonceKt;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = null
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Lkotlin/coroutines/jvm/internal/SuspendLambda;",
        "Lkotlin/jvm/functions/Function2<",
        "Lkotlinx/coroutines/CoroutineScope;",
        "Lkotlin/coroutines/Continuation<",
        "-",
        "Lkotlin/Unit;",
        ">;",
        "Ljava/lang/Object;",
        ">;"
    }
.end annotation

.annotation runtime Lkotlin/Metadata;
    d1 = {
        "\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"
    }
    d2 = {
        "<anonymous>",
        "",
        "Lkotlinx/coroutines/CoroutineScope;"
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
    c = "io.ktor.util.NonceKt$nonceGeneratorJob$1"
    f = "Nonce.kt"
    i = {
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0
    }
    l = {
        0x4c
    }
    m = "invokeSuspend"
    n = {
        "seedChannel",
        "previousRoundNonceList",
        "secureInstance",
        "weakRandom",
        "secureBytes",
        "weakBytes",
        "randomNonceList",
        "lastReseed",
        "currentTime",
        "index"
    }
    s = {
        "L$0",
        "L$1",
        "L$2",
        "L$3",
        "L$4",
        "L$5",
        "L$6",
        "J$0",
        "J$1",
        "I$0"
    }
    v = 0x1
.end annotation


# instance fields
.field I$0:I

.field I$1:I

.field J$0:J

.field J$1:J

.field L$0:Ljava/lang/Object;

.field L$1:Ljava/lang/Object;

.field L$2:Ljava/lang/Object;

.field L$3:Ljava/lang/Object;

.field L$4:Ljava/lang/Object;

.field L$5:Ljava/lang/Object;

.field L$6:Ljava/lang/Object;

.field label:I


# direct methods
.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 3
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lloops/TestTryProtectedCountedSendResumeLatch;",
            ">;)V"
        }
    .end annotation

    .line 1
    const/4 v0, 0x2

    .line 2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V

    .line 3
    .line 4
    .line 5
    return-void
.end method


# virtual methods
.method public final create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;
    .registers 4
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
    new-instance p1, Lloops/TestTryProtectedCountedSendResumeLatch;

    .line 2
    .line 3
    const/4 v0, 0x2

    .line 4
    invoke-direct {p1, v0, p2}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V

    .line 5
    .line 6
    .line 7
    return-object p1
.end method

.method public bridge synthetic invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    .line 1
    check-cast p1, Lkotlinx/coroutines/CoroutineScope;

    check-cast p2, Lkotlin/coroutines/Continuation;

    invoke-virtual {p0, p1, p2}, Lloops/TestTryProtectedCountedSendResumeLatch;->invoke(Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public final invoke(Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lkotlinx/coroutines/CoroutineScope;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lkotlin/Unit;",
            ">;)",
            "Ljava/lang/Object;"
        }
    .end annotation

    .line 2
    invoke-virtual {p0, p1, p2}, Lloops/TestTryProtectedCountedSendResumeLatch;->create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;

    move-result-object p1

    check-cast p1, Lloops/TestTryProtectedCountedSendResumeLatch;

    sget-object p2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    invoke-virtual {p1, p2}, Lloops/TestTryProtectedCountedSendResumeLatch;->invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 26

    .line 1
    move-object/from16 v1, p0

    .line 2
    .line 3
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    .line 4
    .line 5
    .line 6
    move-result-object v0

    .line 7
    iget v2, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->label:I

    .line 8
    .line 9
    const/4 v3, 0x1

    .line 10
    if-eqz v2, :cond_56

    .line 11
    .line 12
    if-ne v2, v3, :cond_49

    .line 13
    .line 14
    iget v2, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->I$1:I

    .line 15
    .line 16
    iget v4, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->I$0:I

    .line 17
    .line 18
    iget-wide v5, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->J$1:J

    .line 19
    .line 20
    iget-wide v7, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->J$0:J

    .line 21
    .line 22
    iget-object v9, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$6:Ljava/lang/Object;

    .line 23
    .line 24
    check-cast v9, Ljava/util/List;

    .line 25
    .line 26
    iget-object v10, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$5:Ljava/lang/Object;

    .line 27
    .line 28
    check-cast v10, [B

    .line 29
    .line 30
    iget-object v11, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$4:Ljava/lang/Object;

    .line 31
    .line 32
    check-cast v11, [B

    .line 33
    .line 34
    iget-object v12, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$3:Ljava/lang/Object;

    .line 35
    .line 36
    check-cast v12, Ljava/security/SecureRandom;

    .line 37
    .line 38
    iget-object v13, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$2:Ljava/lang/Object;

    .line 39
    .line 40
    check-cast v13, Ljava/security/SecureRandom;

    .line 41
    .line 42
    iget-object v14, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$1:Ljava/lang/Object;

    .line 43
    .line 44
    check-cast v14, Ljava/util/ArrayList;

    .line 45
    .line 46
    iget-object v15, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$0:Ljava/lang/Object;

    .line 47
    .line 48
    check-cast v15, Lkotlinx/coroutines/channels/Channel;

    .line 49
    .line 50
    :try_start_31
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_34
    .catchall {:try_start_31 .. :try_end_34} :catchall_46

    .line 51
    .line 52
    .line 53
    move/from16 v17, v2

    .line 54
    .line 55
    move v2, v3

    .line 56
    move-wide/from16 v20, v7

    .line 57
    .line 58
    move-object v8, v11

    .line 59
    move-object v7, v12

    .line 60
    move-object v11, v15

    .line 61
    move-wide/from16 v22, v5

    .line 62
    .line 63
    move-object v6, v13

    .line 64
    move-wide/from16 v12, v22

    .line 65
    .line 66
    move-object v5, v14

    .line 67
    move-wide/from16 v14, v20

    .line 68
    .line 69
    goto/16 :goto_10d

    .line 70
    .line 71
    :catchall_46
    move-exception v0

    .line 72
    goto/16 :goto_13a

    .line 73
    .line 74
    :cond_49
    new-instance v0, Ljava/lang/IllegalStateException;

    .line 75
    .line 76
    const v2, 0x624cfed3

    invoke-static {v2}, Lfixtures/obfuscation/StringDecoder;->decode(I)Ljava/lang/String;

    move-result-object v2

    .line 77
    .line 78
    invoke-direct {v0, v2}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    .line 79
    .line 80
    .line 81
    throw v0

    .line 82
    :cond_56
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 83
    .line 84
    .line 85
    invoke-static {}, Lio/ktor/util/NonceKt;->getSeedChannel()Lkotlinx/coroutines/channels/Channel;

    .line 86
    .line 87
    .line 88
    move-result-object v2

    .line 89
    new-instance v4, Ljava/util/ArrayList;

    .line 90
    .line 91
    invoke-direct {v4}, Ljava/util/ArrayList;-><init>()V

    .line 92
    .line 93
    .line 94
    # invokes: Lio/ktor/util/NonceKt;->d()Ljava/security/SecureRandom;
    invoke-static {}, Lio/ktor/util/NonceKt;->access$lookupSecureRandom()Ljava/security/SecureRandom;

    .line 95
    .line 96
    .line 97
    move-result-object v5

    .line 98
    const v6, -0x1321ce36

    invoke-static {v6}, Lfixtures/obfuscation/StringDecoder;->decode(I)Ljava/lang/String;

    move-result-object v6

    .line 99
    .line 100
    invoke-static {v6}, Ljava/security/SecureRandom;->getInstance(Ljava/lang/String;)Ljava/security/SecureRandom;

    .line 101
    .line 102
    .line 103
    move-result-object v6

    .line 104
    const/16 v7, 0x80

    .line 105
    .line 106
    new-array v8, v7, [B

    .line 107
    .line 108
    const/16 v9, 0x200

    .line 109
    .line 110
    new-array v9, v9, [B

    .line 111
    .line 112
    invoke-virtual {v5, v7}, Ljava/security/SecureRandom;->generateSeed(I)[B

    .line 113
    .line 114
    .line 115
    move-result-object v7

    .line 116
    invoke-virtual {v6, v7}, Ljava/security/SecureRandom;->setSeed([B)V

    .line 117
    .line 118
    .line 119
    const-wide/16 v10, 0x0

    .line 120
    .line 121
    move-object v15, v2

    .line 122
    :goto_83
    :try_start_83
    invoke-virtual {v5, v8}, Ljava/security/SecureRandom;->nextBytes([B)V

    .line 123
    .line 124
    .line 125
    invoke-virtual {v6, v9}, Ljava/security/SecureRandom;->nextBytes([B)V

    .line 126
    .line 127
    .line 128
    array-length v2, v8

    .line 129
    const/4 v7, 0x0

    .line 130
    move v12, v7

    .line 131
    :goto_8c
    if-ge v12, v2, :cond_97

    .line 132
    .line 133
    mul-int/lit8 v13, v12, 0x4

    .line 134
    .line 135
    aget-byte v14, v8, v12

    .line 136
    .line 137
    aput-byte v14, v9, v13

    .line 138
    .line 139
    add-int/lit8 v12, v12, 0x1

    .line 140
    .line 141
    goto :goto_8c

    .line 142
    :cond_97
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J

    .line 143
    .line 144
    .line 145
    move-result-wide v12

    .line 146
    sub-long v16, v12, v10

    .line 147
    .line 148
    const-wide/16 v18, 0x7530

    .line 149
    .line 150
    cmp-long v2, v16, v18

    .line 151
    .line 152
    if-lez v2, :cond_b1

    .line 153
    .line 154
    sub-long/2addr v10, v12

    .line 155
    invoke-virtual {v6, v10, v11}, Ljava/security/SecureRandom;->setSeed(J)V

    .line 156
    .line 157
    .line 158
    array-length v2, v8

    .line 159
    invoke-virtual {v5, v2}, Ljava/security/SecureRandom;->generateSeed(I)[B

    .line 160
    .line 161
    .line 162
    move-result-object v2

    .line 163
    invoke-virtual {v6, v2}, Ljava/security/SecureRandom;->setSeed([B)V

    .line 164
    .line 165
    .line 166
    move-wide v10, v12

    .line 167
    goto :goto_b4

    .line 168
    :cond_b1
    invoke-virtual {v6, v8}, Ljava/security/SecureRandom;->setSeed([B)V

    .line 169
    .line 170
    .line 171
    :goto_b4
    invoke-static {v9}, Lio/ktor/util/CryptoKt;->hex([B)Ljava/lang/String;

    .line 172
    .line 173
    .line 174
    move-result-object v2

    .line 175
    const/16 v14, 0x10

    .line 176
    .line 177
    invoke-static {v2, v14}, Lkotlin/text/f;->chunked(Ljava/lang/CharSequence;I)Ljava/util/List;

    .line 178
    .line 179
    .line 180
    move-result-object v2

    .line 181
    check-cast v2, Ljava/util/Collection;

    .line 182
    .line 183
    invoke-static {v2, v4}, Lkotlin/collections/q;->plus(Ljava/util/Collection;Ljava/lang/Iterable;)Ljava/util/List;

    .line 184
    .line 185
    .line 186
    move-result-object v2

    .line 187
    check-cast v2, Ljava/lang/Iterable;

    .line 188
    .line 189
    invoke-static {v6}, Lkotlin/jvm/internal/Intrinsics;->checkNotNull(Ljava/lang/Object;)V

    .line 190
    .line 191
    .line 192
    invoke-static {v2, v6}, Lkotlin/collections/q;->shuffled(Ljava/lang/Iterable;Ljava/util/Random;)Ljava/util/List;

    .line 193
    .line 194
    .line 195
    move-result-object v2

    .line 196
    invoke-interface {v2}, Ljava/util/List;->size()I

    .line 197
    .line 198
    .line 199
    move-result v14

    .line 200
    div-int/lit8 v14, v14, 0x2
    :try_end_d3
    .catchall {:try_start_83 .. :try_end_d3} :catchall_46

    .line 201
    .line 202
    move-object/from16 v20, v5

    .line 203
    .line 204
    move-object v5, v4

    .line 205
    move v4, v7

    .line 206
    move-object v7, v6

    .line 207
    move-object/from16 v6, v20

    .line 208
    .line 209
    move-object/from16 v20, v9

    .line 210
    .line 211
    move-object v9, v2

    .line 212
    move v2, v14

    .line 213
    move-wide/from16 v21, v10

    .line 214
    .line 215
    move-object/from16 v10, v20

    .line 216
    .line 217
    move-object v11, v15

    .line 218
    move-wide/from16 v14, v21

    .line 219
    .line 220
    :goto_e5
    if-ge v4, v2, :cond_115

    .line 221
    .line 222
    :try_start_e7
    invoke-interface {v9, v4}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 223
    .line 224
    .line 225
    move-result-object v3

    .line 226
    iput-object v11, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$0:Ljava/lang/Object;

    .line 227
    .line 228
    iput-object v5, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$1:Ljava/lang/Object;

    .line 229
    .line 230
    iput-object v6, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$2:Ljava/lang/Object;

    .line 231
    .line 232
    iput-object v7, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$3:Ljava/lang/Object;

    .line 233
    .line 234
    iput-object v8, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$4:Ljava/lang/Object;

    .line 235
    .line 236
    iput-object v10, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$5:Ljava/lang/Object;

    .line 237
    .line 238
    iput-object v9, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->L$6:Ljava/lang/Object;

    .line 239
    .line 240
    iput-wide v14, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->J$0:J

    .line 241
    .line 242
    iput-wide v12, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->J$1:J

    .line 243
    .line 244
    iput v4, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->I$0:I

    .line 245
    .line 246
    iput v2, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->I$1:I

    .line 247
    .line 248
    move/from16 v17, v2

    .line 249
    .line 250
    const/4 v2, 0x1

    .line 251
    iput v2, v1, Lloops/TestTryProtectedCountedSendResumeLatch;->label:I

    .line 252
    .line 253
    invoke-interface {v11, v3, v1}, Lkotlinx/coroutines/channels/SendChannel;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 254
    .line 255
    .line 256
    move-result-object v3

    .line 257
    if-ne v3, v0, :cond_10d

    .line 258
    .line 259
    return-object v0

    .line 260
    :cond_10d
    :goto_10d
    add-int/2addr v4, v2

    .line 261
    move v3, v2

    .line 262
    move/from16 v2, v17

    .line 263
    .line 264
    goto :goto_e5

    .line 265
    :catchall_112
    move-exception v0

    .line 266
    move-object v15, v11

    .line 267
    goto :goto_13a

    .line 268
    :cond_115
    invoke-virtual {v5}, Ljava/util/ArrayList;->clear()V

    .line 269
    .line 270
    .line 271
    invoke-interface {v9}, Ljava/util/List;->size()I

    .line 272
    .line 273
    .line 274
    move-result v2

    .line 275
    div-int/lit8 v2, v2, 0x2

    .line 276
    .line 277
    invoke-interface {v9}, Ljava/util/List;->size()I

    .line 278
    .line 279
    .line 280
    move-result v3

    .line 281
    :goto_122
    if-ge v2, v3, :cond_12e

    .line 282
    .line 283
    invoke-interface {v9, v2}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 284
    .line 285
    .line 286
    move-result-object v4

    .line 287
    invoke-virtual {v5, v4}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
    :try_end_12b
    .catchall {:try_start_e7 .. :try_end_12b} :catchall_112

    .line 288
    .line 289
    .line 290
    add-int/lit8 v2, v2, 0x1

    .line 291
    .line 292
    goto :goto_122

    .line 293
    :cond_12e
    move-object v4, v5

    .line 294
    move-object v5, v6

    .line 295
    move-object v6, v7

    .line 296
    move-object v9, v10

    .line 297
    const/4 v3, 0x1

    .line 298
    move-wide/from16 v20, v14

    .line 299
    .line 300
    move-object v15, v11

    .line 301
    move-wide/from16 v10, v20

    .line 302
    .line 303
    goto/16 :goto_83

    .line 304
    .line 305
    :goto_13a
    const/4 v2, 0x0

    .line 306
    :try_start_13b
    invoke-interface {v15, v0}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z
    :try_end_13e
    .catchall {:try_start_13b .. :try_end_13e} :catchall_145

    .line 307
    .line 308
    .line 309
    const/4 v3, 0x1

    .line 310
    invoke-static {v15, v2, v3, v2}, Lkotlinx/coroutines/channels/SendChannel$DefaultImpls;->close$default(Lkotlinx/coroutines/channels/SendChannel;Ljava/lang/Throwable;ILjava/lang/Object;)Z

    .line 311
    .line 312
    .line 313
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    .line 314
    .line 315
    return-object v0

    .line 316
    :catchall_145
    move-exception v0

    .line 317
    const/4 v3, 0x1

    .line 318
    invoke-static {v15, v2, v3, v2}, Lkotlinx/coroutines/channels/SendChannel$DefaultImpls;->close$default(Lkotlinx/coroutines/channels/SendChannel;Ljava/lang/Throwable;ILjava/lang/Object;)Z

    .line 319
    .line 320
    .line 321
    throw v0
.end method
