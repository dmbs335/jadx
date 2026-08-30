.class public final Lloops/TestTryProtectedCoroutineReceiveFlushLoop;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;
.source "RawWebSocketCommon.kt"

# interfaces
.implements Lkotlin/jvm/functions/Function2;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lio/ktor/websocket/RawWebSocketCommon;-><init>(Lio/ktor/utils/io/ByteReadChannel;Lio/ktor/utils/io/ByteWriteChannel;JZLkotlin/coroutines/CoroutineContext;)V
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
    c = "io.ktor.websocket.RawWebSocketCommon$writerJob$1"
    f = "RawWebSocketCommon.kt"
    i = {
        0x1,
        0x2
    }
    l = {
        0x3e,
        0x40,
        0x41,
        0x54,
        0x54,
        0x54,
        0x54
    }
    m = "invokeSuspend"
    n = {
        "message",
        "message"
    }
    s = {
        "L$0",
        "L$0"
    }
    v = 0x1
.end annotation


# instance fields
.field L$0:Ljava/lang/Object;

.field label:I

.field final synthetic this$0:Lio/ktor/websocket/RawWebSocketCommon;


# direct methods
.method public constructor <init>(Lio/ktor/websocket/RawWebSocketCommon;Lkotlin/coroutines/Continuation;)V
    .registers 3
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lio/ktor/websocket/RawWebSocketCommon;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lloops/TestTryProtectedCoroutineReceiveFlushLoop;",
            ">;)V"
        }
    .end annotation

    .line 1
    iput-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 2
    .line 3
    const/4 p1, 0x2

    .line 4
    invoke-direct {p0, p1, p2}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V

    .line 5
    .line 6
    .line 7
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
    new-instance p1, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;

    .line 2
    .line 3
    iget-object v0, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 4
    .line 5
    invoke-direct {p1, v0, p2}, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;-><init>(Lio/ktor/websocket/RawWebSocketCommon;Lkotlin/coroutines/Continuation;)V

    .line 6
    .line 7
    .line 8
    return-object p1
.end method

.method public bridge synthetic invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    .line 1
    check-cast p1, Lkotlinx/coroutines/CoroutineScope;

    check-cast p2, Lkotlin/coroutines/Continuation;

    invoke-virtual {p0, p1, p2}, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->invoke(Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

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
    invoke-virtual {p0, p1, p2}, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;

    move-result-object p1

    check-cast p1, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;

    sget-object p2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    invoke-virtual {p1, p2}, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10

    .line 1
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    .line 2
    .line 3
    .line 4
    move-result-object v0

    .line 5
    iget v1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->label:I

    .line 6
    .line 7
    const/4 v2, 0x1

    .line 8
    const v3, -0x14e86a1f

    invoke-static {v3}, Lfixtures/obfuscation/StringDecoder;->ʒʔɏ̌(I)Ljava/lang/String;

    move-result-object v3

    .line 9
    .line 10
    const/4 v4, 0x0

    .line 11
    packed-switch v1, :pswitch_data_17a

    .line 12
    .line 13
    .line 14
    new-instance p1, Ljava/lang/IllegalStateException;

    .line 15
    .line 16
    const v0, 0x624cfed3

    invoke-static {v0}, Lfixtures/obfuscation/StringDecoder;->˔˓̏ʍ(I)Ljava/lang/String;

    move-result-object v0

    .line 17
    .line 18
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    .line 19
    .line 20
    .line 21
    throw p1

    .line 22
    :pswitch_1f
    iget-object v0, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->L$0:Ljava/lang/Object;

    .line 23
    .line 24
    check-cast v0, Ljava/lang/Throwable;

    .line 25
    .line 26
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 27
    .line 28
    .line 29
    goto/16 :goto_179

    .line 30
    .line 31
    :pswitch_28
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 32
    .line 33
    .line 34
    goto/16 :goto_138

    .line 35
    .line 36
    :pswitch_2d
    iget-object v1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->L$0:Ljava/lang/Object;

    .line 37
    .line 38
    :try_start_2f
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_32
    .catch Lio/ktor/util/cio/ChannelWriteException; {:try_start_2f .. :try_end_32} :catch_36
    .catchall {:try_start_2f .. :try_end_32} :catchall_33

    .line 39
    .line 40
    .line 41
    goto :goto_8c

    .line 42
    :catchall_33
    move-exception p1

    .line 43
    goto/16 :goto_e0

    .line 44
    .line 45
    :catch_36
    move-exception p1

    .line 46
    goto/16 :goto_10a

    .line 47
    .line 48
    :pswitch_39
    iget-object v1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->L$0:Ljava/lang/Object;

    .line 49
    .line 50
    :try_start_3b
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 51
    .line 52
    .line 53
    goto :goto_79

    .line 54
    :pswitch_3f
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_42
    .catch Lio/ktor/util/cio/ChannelWriteException; {:try_start_3b .. :try_end_42} :catch_36
    .catchall {:try_start_3b .. :try_end_42} :catchall_33

    .line 55
    .line 56
    .line 57
    goto :goto_58

    .line 58
    :pswitch_43
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 59
    .line 60
    .line 61
    :cond_46
    :goto_46
    :try_start_46
    iget-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 62
    .line 63
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->j:Lkotlinx/coroutines/channels/Channel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_outgoing$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;

    .line 64
    .line 65
    .line 66
    move-result-object p1

    .line 67
    iput-object v4, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->L$0:Ljava/lang/Object;

    .line 68
    .line 69
    iput v2, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->label:I

    .line 70
    .line 71
    invoke-interface {p1, p0}, Lkotlinx/coroutines/channels/ReceiveChannel;->receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 72
    .line 73
    .line 74
    move-result-object p1

    .line 75
    if-ne p1, v0, :cond_58

    .line 76
    .line 77
    goto/16 :goto_177

    .line 78
    .line 79
    :cond_58
    :goto_58
    instance-of v1, p1, Lio/ktor/websocket/Frame;

    .line 80
    .line 81
    if-eqz v1, :cond_b9

    .line 82
    .line 83
    iget-object v1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 84
    .line 85
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->e:Lio/ktor/utils/io/ByteWriteChannel;
    invoke-static {v1}, Lio/ktor/websocket/RawWebSocketCommon;->access$getOutput$p(Lio/ktor/websocket/RawWebSocketCommon;)Lio/ktor/utils/io/ByteWriteChannel;

    .line 86
    .line 87
    .line 88
    move-result-object v1

    .line 89
    move-object v5, p1

    .line 90
    check-cast v5, Lio/ktor/websocket/Frame;

    .line 91
    .line 92
    iget-object v6, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 93
    .line 94
    invoke-virtual {v6}, Lio/ktor/websocket/RawWebSocketCommon;->getMasking()Z

    .line 95
    .line 96
    .line 97
    move-result v6

    .line 98
    iput-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->L$0:Ljava/lang/Object;

    .line 99
    .line 100
    const/4 v7, 0x2

    .line 101
    iput v7, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->label:I

    .line 102
    .line 103
    invoke-static {v1, v5, v6, p0}, Lio/ktor/websocket/RawWebSocketCommonKt;->writeFrame(Lio/ktor/utils/io/ByteWriteChannel;Lio/ktor/websocket/Frame;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 104
    .line 105
    .line 106
    move-result-object v1

    .line 107
    if-ne v1, v0, :cond_78

    .line 108
    .line 109
    goto/16 :goto_177

    .line 110
    .line 111
    :cond_78
    move-object v1, p1

    .line 112
    :goto_79
    iget-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 113
    .line 114
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->e:Lio/ktor/utils/io/ByteWriteChannel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$getOutput$p(Lio/ktor/websocket/RawWebSocketCommon;)Lio/ktor/utils/io/ByteWriteChannel;

    .line 115
    .line 116
    .line 117
    move-result-object p1

    .line 118
    iput-object v1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->L$0:Ljava/lang/Object;

    .line 119
    .line 120
    const/4 v5, 0x3

    .line 121
    iput v5, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->label:I

    .line 122
    .line 123
    invoke-interface {p1, p0}, Lio/ktor/utils/io/ByteWriteChannel;->flush(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 124
    .line 125
    .line 126
    move-result-object p1

    .line 127
    if-ne p1, v0, :cond_8c

    .line 128
    .line 129
    goto/16 :goto_177

    .line 130
    .line 131
    :cond_8c
    :goto_8c
    instance-of p1, v1, Lio/ktor/websocket/Frame$Close;

    .line 132
    .line 133
    if-eqz p1, :cond_46

    .line 134
    .line 135
    iget-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 136
    .line 137
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->j:Lkotlinx/coroutines/channels/Channel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_outgoing$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;

    .line 138
    .line 139
    .line 140
    move-result-object p1

    .line 141
    invoke-static {p1, v4, v2, v4}, Lkotlinx/coroutines/channels/SendChannel$DefaultImpls;->close$default(Lkotlinx/coroutines/channels/SendChannel;Ljava/lang/Throwable;ILjava/lang/Object;)Z
    :try_end_99
    .catch Lio/ktor/util/cio/ChannelWriteException; {:try_start_46 .. :try_end_99} :catch_36
    .catchall {:try_start_46 .. :try_end_99} :catchall_33

    .line 142
    .line 143
    .line 144
    iget-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 145
    .line 146
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->j:Lkotlinx/coroutines/channels/Channel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_outgoing$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;

    .line 147
    .line 148
    .line 149
    move-result-object p1

    .line 150
    invoke-static {v3, v4}, Lkotlinx/coroutines/ExceptionsKt;->CancellationException(Ljava/lang/String;Ljava/lang/Throwable;)Ljava/util/concurrent/CancellationException;

    .line 151
    .line 152
    .line 153
    move-result-object v1

    .line 154
    invoke-interface {p1, v1}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z

    .line 155
    .line 156
    .line 157
    iget-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 158
    .line 159
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->e:Lio/ktor/utils/io/ByteWriteChannel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$getOutput$p(Lio/ktor/websocket/RawWebSocketCommon;)Lio/ktor/utils/io/ByteWriteChannel;

    .line 160
    .line 161
    .line 162
    move-result-object p1

    .line 163
    iput-object v4, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->L$0:Ljava/lang/Object;

    .line 164
    .line 165
    const/4 v1, 0x4

    .line 166
    iput v1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->label:I

    .line 167
    .line 168
    invoke-interface {p1, p0}, Lio/ktor/utils/io/ByteWriteChannel;->flushAndClose(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 169
    .line 170
    .line 171
    move-result-object p1

    .line 172
    if-ne p1, v0, :cond_138

    .line 173
    .line 174
    goto/16 :goto_177

    .line 175
    .line 176
    :cond_b9
    :try_start_b9
    instance-of v1, p1, Lio/ktor/websocket/RawWebSocketCommon$FlushRequest;

    .line 177
    .line 178
    if-eqz v1, :cond_c8

    .line 179
    .line 180
    check-cast p1, Lio/ktor/websocket/RawWebSocketCommon$FlushRequest;

    .line 181
    .line 182
    invoke-virtual {p1}, Lio/ktor/websocket/RawWebSocketCommon$FlushRequest;->complete()Z

    .line 183
    .line 184
    .line 185
    move-result p1

    .line 186
    invoke-static {p1}, Lkotlin/coroutines/jvm/internal/Boxing;->boxBoolean(Z)Ljava/lang/Boolean;

    .line 187
    .line 188
    .line 189
    goto/16 :goto_46

    .line 190
    .line 191
    :cond_c8
    new-instance v1, Ljava/lang/IllegalArgumentException;

    .line 192
    .line 193
    new-instance v2, Ljava/lang/StringBuilder;

    .line 194
    .line 195
    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    .line 196
    .line 197
    .line 198
    const-string/jumbo v5, "unknown message "

    .line 199
    .line 200
    .line 201
    invoke-virtual {v2, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 202
    .line 203
    .line 204
    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    .line 205
    .line 206
    .line 207
    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 208
    .line 209
    .line 210
    move-result-object p1

    .line 211
    invoke-direct {v1, p1}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    .line 212
    .line 213
    .line 214
    throw v1
    :try_end_e0
    .catch Lio/ktor/util/cio/ChannelWriteException; {:try_start_b9 .. :try_end_e0} :catch_36
    .catchall {:try_start_b9 .. :try_end_e0} :catchall_33

    .line 215
    :goto_e0
    :try_start_e0
    iget-object v1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 216
    .line 217
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->j:Lkotlinx/coroutines/channels/Channel;
    invoke-static {v1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_outgoing$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;

    .line 218
    .line 219
    .line 220
    move-result-object v1

    .line 221
    invoke-interface {v1, p1}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z
    :try_end_e9
    .catchall {:try_start_e0 .. :try_end_e9} :catchall_108

    .line 222
    .line 223
    .line 224
    iget-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 225
    .line 226
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->j:Lkotlinx/coroutines/channels/Channel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_outgoing$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;

    .line 227
    .line 228
    .line 229
    move-result-object p1

    .line 230
    invoke-static {v3, v4}, Lkotlinx/coroutines/ExceptionsKt;->CancellationException(Ljava/lang/String;Ljava/lang/Throwable;)Ljava/util/concurrent/CancellationException;

    .line 231
    .line 232
    .line 233
    move-result-object v1

    .line 234
    invoke-interface {p1, v1}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z

    .line 235
    .line 236
    .line 237
    iget-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 238
    .line 239
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->e:Lio/ktor/utils/io/ByteWriteChannel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$getOutput$p(Lio/ktor/websocket/RawWebSocketCommon;)Lio/ktor/utils/io/ByteWriteChannel;

    .line 240
    .line 241
    .line 242
    move-result-object p1

    .line 243
    iput-object v4, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->L$0:Ljava/lang/Object;

    .line 244
    .line 245
    const/4 v1, 0x6

    .line 246
    iput v1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->label:I

    .line 247
    .line 248
    invoke-interface {p1, p0}, Lio/ktor/utils/io/ByteWriteChannel;->flushAndClose(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 249
    .line 250
    .line 251
    move-result-object p1

    .line 252
    if-ne p1, v0, :cond_138

    .line 253
    .line 254
    goto :goto_177

    .line 255
    :catchall_108
    move-exception p1

    .line 256
    goto :goto_159

    .line 257
    :goto_10a
    :try_start_10a
    iget-object v1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 258
    .line 259
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->j:Lkotlinx/coroutines/channels/Channel;
    invoke-static {v1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_outgoing$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;

    .line 260
    .line 261
    .line 262
    move-result-object v1

    .line 263
    const-string v2, "Failed to write to WebSocket."

    .line 264
    .line 265
    invoke-static {v2, p1}, Lkotlinx/coroutines/ExceptionsKt;->CancellationException(Ljava/lang/String;Ljava/lang/Throwable;)Ljava/util/concurrent/CancellationException;

    .line 266
    .line 267
    .line 268
    move-result-object p1

    .line 269
    invoke-interface {v1, p1}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z
    :try_end_119
    .catchall {:try_start_10a .. :try_end_119} :catchall_108

    .line 270
    .line 271
    .line 272
    iget-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 273
    .line 274
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->j:Lkotlinx/coroutines/channels/Channel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_outgoing$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;

    .line 275
    .line 276
    .line 277
    move-result-object p1

    .line 278
    invoke-static {v3, v4}, Lkotlinx/coroutines/ExceptionsKt;->CancellationException(Ljava/lang/String;Ljava/lang/Throwable;)Ljava/util/concurrent/CancellationException;

    .line 279
    .line 280
    .line 281
    move-result-object v1

    .line 282
    invoke-interface {p1, v1}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z

    .line 283
    .line 284
    .line 285
    iget-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 286
    .line 287
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->e:Lio/ktor/utils/io/ByteWriteChannel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$getOutput$p(Lio/ktor/websocket/RawWebSocketCommon;)Lio/ktor/utils/io/ByteWriteChannel;

    .line 288
    .line 289
    .line 290
    move-result-object p1

    .line 291
    iput-object v4, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->L$0:Ljava/lang/Object;

    .line 292
    .line 293
    const/4 v1, 0x5

    .line 294
    iput v1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->label:I

    .line 295
    .line 296
    invoke-interface {p1, p0}, Lio/ktor/utils/io/ByteWriteChannel;->flushAndClose(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 297
    .line 298
    .line 299
    move-result-object p1

    .line 300
    if-ne p1, v0, :cond_138

    .line 301
    .line 302
    goto :goto_177

    .line 303
    :cond_138
    :goto_138
    iget-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 304
    .line 305
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->j:Lkotlinx/coroutines/channels/Channel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_outgoing$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;

    .line 306
    .line 307
    .line 308
    move-result-object p1

    .line 309
    invoke-interface {p1}, Lkotlinx/coroutines/channels/ReceiveChannel;->tryReceive-PtdJZtk()Ljava/lang/Object;

    .line 310
    .line 311
    .line 312
    move-result-object p1

    .line 313
    invoke-static {p1}, Lkotlinx/coroutines/channels/ChannelResult;->getOrNull-impl(Ljava/lang/Object;)Ljava/lang/Object;

    .line 314
    .line 315
    .line 316
    move-result-object p1

    .line 317
    if-nez p1, :cond_14b

    .line 318
    .line 319
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    .line 320
    .line 321
    return-object p1

    .line 322
    :cond_14b
    instance-of v0, p1, Lio/ktor/websocket/RawWebSocketCommon$FlushRequest;

    .line 323
    .line 324
    if-eqz v0, :cond_138

    .line 325
    .line 326
    check-cast p1, Lio/ktor/websocket/RawWebSocketCommon$FlushRequest;

    .line 327
    .line 328
    invoke-virtual {p1}, Lio/ktor/websocket/RawWebSocketCommon$FlushRequest;->complete()Z

    .line 329
    .line 330
    .line 331
    move-result p1

    .line 332
    invoke-static {p1}, Lkotlin/coroutines/jvm/internal/Boxing;->boxBoolean(Z)Ljava/lang/Boolean;

    .line 333
    .line 334
    .line 335
    goto :goto_138

    .line 336
    :goto_159
    iget-object v1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 337
    .line 338
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->j:Lkotlinx/coroutines/channels/Channel;
    invoke-static {v1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_outgoing$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;

    .line 339
    .line 340
    .line 341
    move-result-object v1

    .line 342
    invoke-static {v3, v4}, Lkotlinx/coroutines/ExceptionsKt;->CancellationException(Ljava/lang/String;Ljava/lang/Throwable;)Ljava/util/concurrent/CancellationException;

    .line 343
    .line 344
    .line 345
    move-result-object v2

    .line 346
    invoke-interface {v1, v2}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z

    .line 347
    .line 348
    .line 349
    iget-object v1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;

    .line 350
    .line 351
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->e:Lio/ktor/utils/io/ByteWriteChannel;
    invoke-static {v1}, Lio/ktor/websocket/RawWebSocketCommon;->access$getOutput$p(Lio/ktor/websocket/RawWebSocketCommon;)Lio/ktor/utils/io/ByteWriteChannel;

    .line 352
    .line 353
    .line 354
    move-result-object v1

    .line 355
    iput-object p1, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->L$0:Ljava/lang/Object;

    .line 356
    .line 357
    const/4 v2, 0x7

    .line 358
    iput v2, p0, Lloops/TestTryProtectedCoroutineReceiveFlushLoop;->label:I

    .line 359
    .line 360
    invoke-interface {v1, p0}, Lio/ktor/utils/io/ByteWriteChannel;->flushAndClose(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 361
    .line 362
    .line 363
    move-result-object v1

    .line 364
    if-ne v1, v0, :cond_178

    .line 365
    .line 366
    :goto_177
    return-object v0

    .line 367
    :cond_178
    move-object v0, p1

    .line 368
    :goto_179
    throw v0

    .line 369
    :pswitch_data_17a
    .packed-switch 0x0
        :pswitch_43
        :pswitch_3f
        :pswitch_39
        :pswitch_2d
        :pswitch_28
        :pswitch_28
        :pswitch_28
        :pswitch_1f
    .end packed-switch
.end method
