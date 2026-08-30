.class public final Lloops/TestTryProtectedWebSocketChannelIterator;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;
.source "OkHttpWebsocketSession.kt"

# interfaces
.implements Lkotlin/jvm/functions/Function2;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;-><init>(Lokhttp3/OkHttpClient;Lokhttp3/WebSocket$Factory;Lokhttp3/Request;Lkotlin/coroutines/CoroutineContext;)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = null
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Lkotlin/coroutines/jvm/internal/SuspendLambda;",
        "Lkotlin/jvm/functions/Function2<",
        "Lkotlinx/coroutines/channels/ActorScope<",
        "Lio/ktor/websocket/Frame;",
        ">;",
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
        "\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u0008\u0012\u0004\u0012\u00020\u00030\u0002H\n"
    }
    d2 = {
        "<anonymous>",
        "",
        "Lkotlinx/coroutines/channels/ActorScope;",
        "Lio/ktor/websocket/Frame;"
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
    c = "io.ktor.client.engine.okhttp.OkHttpWebsocketSession$outgoing$1"
    f = "OkHttpWebsocketSession.kt"
    i = {
        0x0,
        0x1,
        0x1,
        0x1
    }
    l = {
        0x40,
        0x44
    }
    m = "invokeSuspend"
    n = {
        "$this$actor",
        "$this$actor",
        "websocket",
        "closeReason"
    }
    s = {
        "L$0",
        "L$0",
        "L$1",
        "L$2"
    }
    v = 0x1
.end annotation


# instance fields
.field final synthetic $engineRequest:Lokhttp3/Request;

.field private synthetic L$0:Ljava/lang/Object;

.field L$1:Ljava/lang/Object;

.field L$2:Ljava/lang/Object;

.field L$3:Ljava/lang/Object;

.field label:I

.field final synthetic this$0:Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;


# direct methods
.method public constructor <init>(Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;Lokhttp3/Request;Lkotlin/coroutines/Continuation;)V
    .registers 4
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;",
            "Lokhttp3/Request;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lloops/TestTryProtectedWebSocketChannelIterator;",
            ">;)V"
        }
    .end annotation

    .line 1
    iput-object p1, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->this$0:Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;

    .line 2
    .line 3
    iput-object p2, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->$engineRequest:Lokhttp3/Request;

    .line 4
    .line 5
    const/4 p1, 0x2

    .line 6
    invoke-direct {p0, p1, p3}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V

    .line 7
    .line 8
    .line 9
    return-void
.end method


# virtual methods
.method public final create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;
    .registers 6
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
    new-instance v0, Lloops/TestTryProtectedWebSocketChannelIterator;

    .line 2
    .line 3
    iget-object v1, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->this$0:Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;

    .line 4
    .line 5
    iget-object v2, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->$engineRequest:Lokhttp3/Request;

    .line 6
    .line 7
    invoke-direct {v0, v1, v2, p2}, Lloops/TestTryProtectedWebSocketChannelIterator;-><init>(Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;Lokhttp3/Request;Lkotlin/coroutines/Continuation;)V

    .line 8
    .line 9
    .line 10
    iput-object p1, v0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$0:Ljava/lang/Object;

    .line 11
    .line 12
    return-object v0
.end method

.method public bridge synthetic invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    .line 1
    check-cast p1, Lkotlinx/coroutines/channels/ActorScope;

    check-cast p2, Lkotlin/coroutines/Continuation;

    invoke-virtual {p0, p1, p2}, Lloops/TestTryProtectedWebSocketChannelIterator;->invoke(Lkotlinx/coroutines/channels/ActorScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public final invoke(Lkotlinx/coroutines/channels/ActorScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lkotlinx/coroutines/channels/ActorScope<",
            "Lio/ktor/websocket/Frame;",
            ">;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lkotlin/Unit;",
            ">;)",
            "Ljava/lang/Object;"
        }
    .end annotation

    .line 2
    invoke-virtual {p0, p1, p2}, Lloops/TestTryProtectedWebSocketChannelIterator;->create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;

    move-result-object p1

    check-cast p1, Lloops/TestTryProtectedWebSocketChannelIterator;

    sget-object p2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    invoke-virtual {p1, p2}, Lloops/TestTryProtectedWebSocketChannelIterator;->invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 12

    .line 1
    iget-object v0, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$0:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lkotlinx/coroutines/channels/ActorScope;

    .line 4
    .line 5
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    .line 6
    .line 7
    .line 8
    move-result-object v1

    .line 9
    iget v2, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->label:I

    .line 10
    .line 11
    const/4 v3, 0x2

    .line 12
    const/4 v4, 0x1

    .line 13
    if-eqz v2, :cond_3e

    .line 14
    .line 15
    if-eq v2, v4, :cond_32

    .line 16
    .line 17
    if-ne v2, v3, :cond_25

    .line 18
    .line 19
    iget-object v2, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$3:Ljava/lang/Object;

    .line 20
    .line 21
    check-cast v2, Lkotlinx/coroutines/channels/ChannelIterator;

    .line 22
    .line 23
    iget-object v4, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$2:Ljava/lang/Object;

    .line 24
    .line 25
    check-cast v4, Lio/ktor/websocket/CloseReason;

    .line 26
    .line 27
    iget-object v5, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$1:Ljava/lang/Object;

    .line 28
    .line 29
    check-cast v5, Lokhttp3/WebSocket;

    .line 30
    .line 31
    :try_start_1e
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_21
    .catchall {:try_start_1e .. :try_end_21} :catchall_22

    .line 32
    .line 33
    .line 34
    goto :goto_89

    .line 35
    :catchall_22
    move-exception p1

    .line 36
    goto/16 :goto_102

    .line 37
    .line 38
    :cond_25
    new-instance p1, Ljava/lang/IllegalStateException;

    .line 39
    .line 40
    const v0, 0x624cfed3

    invoke-static {v0}, Lfixtures/obfuscation/StringDecoder;->decode(I)Ljava/lang/String;

    move-result-object v0

    .line 41
    .line 42
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    .line 43
    .line 44
    .line 45
    throw p1

    .line 46
    :cond_32
    iget-object v2, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$2:Ljava/lang/Object;

    .line 47
    .line 48
    check-cast v2, Lokhttp3/Request;

    .line 49
    .line 50
    iget-object v4, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$1:Ljava/lang/Object;

    .line 51
    .line 52
    check-cast v4, Lokhttp3/WebSocket$Factory;

    .line 53
    .line 54
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 55
    .line 56
    .line 57
    goto :goto_61

    .line 58
    :cond_3e
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 59
    .line 60
    .line 61
    iget-object p1, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->this$0:Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;

    .line 62
    .line 63
    # getter for: Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;->e:Lokhttp3/WebSocket$Factory;
    invoke-static {p1}, Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;->access$getWebSocketFactory$p(Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;)Lokhttp3/WebSocket$Factory;

    .line 64
    .line 65
    .line 66
    move-result-object p1

    .line 67
    iget-object v2, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->$engineRequest:Lokhttp3/Request;

    .line 68
    .line 69
    iget-object v5, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->this$0:Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;

    .line 70
    .line 71
    # getter for: Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;->g:Lkotlinx/coroutines/CompletableDeferred;
    invoke-static {v5}, Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;->access$getSelf$p(Lio/ktor/client/engine/okhttp/OkHttpWebsocketSession;)Lkotlinx/coroutines/CompletableDeferred;

    .line 72
    .line 73
    .line 74
    move-result-object v5

    .line 75
    iput-object v0, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$0:Ljava/lang/Object;

    .line 76
    .line 77
    iput-object p1, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$1:Ljava/lang/Object;

    .line 78
    .line 79
    iput-object v2, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$2:Ljava/lang/Object;

    .line 80
    .line 81
    iput v4, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->label:I

    .line 82
    .line 83
    invoke-interface {v5, p0}, Lkotlinx/coroutines/Deferred;->await(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 84
    .line 85
    .line 86
    move-result-object v4

    .line 87
    if-ne v4, v1, :cond_5e

    .line 88
    .line 89
    goto :goto_88

    .line 90
    :cond_5e
    move-object v9, v4

    .line 91
    move-object v4, p1

    .line 92
    move-object p1, v9

    .line 93
    :goto_61
    check-cast p1, Lokhttp3/WebSocketListener;

    .line 94
    .line 95
    invoke-interface {v4, v2, p1}, Lokhttp3/WebSocket$Factory;->newWebSocket(Lokhttp3/Request;Lokhttp3/WebSocketListener;)Lokhttp3/WebSocket;

    .line 96
    .line 97
    .line 98
    move-result-object v5

    .line 99
    # getter for: Lio/ktor/client/engine/okhttp/OkHttpWebsocketSessionKt;->DEFAULT_CLOSE_REASON_ERROR:Lio/ktor/websocket/CloseReason;
    invoke-static {}, Lio/ktor/client/engine/okhttp/OkHttpWebsocketSessionKt;->access$getDEFAULT_CLOSE_REASON_ERROR$p()Lio/ktor/websocket/CloseReason;

    .line 100
    .line 101
    .line 102
    move-result-object v4

    .line 103
    :try_start_6b
    invoke-interface {v0}, Lkotlinx/coroutines/channels/ActorScope;->getChannel()Lkotlinx/coroutines/channels/Channel;

    .line 104
    .line 105
    .line 106
    move-result-object p1

    .line 107
    invoke-interface {p1}, Lkotlinx/coroutines/channels/ReceiveChannel;->iterator()Lkotlinx/coroutines/channels/ChannelIterator;

    .line 108
    .line 109
    .line 110
    move-result-object p1

    .line 111
    move-object v2, p1

    .line 112
    :goto_74
    invoke-static {v0}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    .line 113
    .line 114
    .line 115
    move-result-object p1

    .line 116
    iput-object p1, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$0:Ljava/lang/Object;

    .line 117
    .line 118
    iput-object v5, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$1:Ljava/lang/Object;

    .line 119
    .line 120
    iput-object v4, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$2:Ljava/lang/Object;

    .line 121
    .line 122
    iput-object v2, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->L$3:Ljava/lang/Object;

    .line 123
    .line 124
    iput v3, p0, Lloops/TestTryProtectedWebSocketChannelIterator;->label:I

    .line 125
    .line 126
    invoke-interface {v2, p0}, Lkotlinx/coroutines/channels/ChannelIterator;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 127
    .line 128
    .line 129
    move-result-object p1

    .line 130
    if-ne p1, v1, :cond_89

    .line 131
    .line 132
    :goto_88
    return-object v1

    .line 133
    :cond_89
    :goto_89
    check-cast p1, Ljava/lang/Boolean;

    .line 134
    .line 135
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z

    .line 136
    .line 137
    .line 138
    move-result p1

    .line 139
    if-eqz p1, :cond_ef

    .line 140
    .line 141
    invoke-interface {v2}, Lkotlinx/coroutines/channels/ChannelIterator;->next()Ljava/lang/Object;

    .line 142
    .line 143
    .line 144
    move-result-object p1

    .line 145
    check-cast p1, Lio/ktor/websocket/Frame;

    .line 146
    .line 147
    instance-of v6, p1, Lio/ktor/websocket/Frame$Binary;

    .line 148
    .line 149
    if-eqz v6, :cond_af

    .line 150
    .line 151
    sget-object v6, Lokio/ByteString;->Companion:Lokio/ByteString$Companion;

    .line 152
    .line 153
    invoke-virtual {p1}, Lio/ktor/websocket/Frame;->getData()[B

    .line 154
    .line 155
    .line 156
    move-result-object v7

    .line 157
    invoke-virtual {p1}, Lio/ktor/websocket/Frame;->getData()[B

    .line 158
    .line 159
    .line 160
    move-result-object p1

    .line 161
    array-length p1, p1

    .line 162
    const/4 v8, 0x0

    .line 163
    invoke-virtual {v6, v7, v8, p1}, Lokio/ByteString$Companion;->of([BII)Lokio/ByteString;

    .line 164
    .line 165
    .line 166
    move-result-object p1

    .line 167
    invoke-interface {v5, p1}, Lokhttp3/WebSocket;->send(Lokio/ByteString;)Z

    .line 168
    .line 169
    .line 170
    goto :goto_74

    .line 171
    :cond_af
    instance-of v6, p1, Lio/ktor/websocket/Frame$Text;

    .line 172
    .line 173
    if-eqz v6, :cond_c2

    .line 174
    .line 175
    new-instance v6, Ljava/lang/String;

    .line 176
    .line 177
    invoke-virtual {p1}, Lio/ktor/websocket/Frame;->getData()[B

    .line 178
    .line 179
    .line 180
    move-result-object p1

    .line 181
    sget-object v7, Lkotlin/text/Charsets;->UTF_8:Ljava/nio/charset/Charset;

    .line 182
    .line 183
    invoke-direct {v6, p1, v7}, Ljava/lang/String;-><init>([BLjava/nio/charset/Charset;)V

    .line 184
    .line 185
    .line 186
    invoke-interface {v5, v6}, Lokhttp3/WebSocket;->send(Ljava/lang/String;)Z

    .line 187
    .line 188
    .line 189
    goto :goto_74

    .line 190
    :cond_c2
    instance-of v0, p1, Lio/ktor/websocket/Frame$Close;

    .line 191
    .line 192
    if-eqz v0, :cond_e9

    .line 193
    .line 194
    check-cast p1, Lio/ktor/websocket/Frame$Close;

    .line 195
    .line 196
    invoke-static {p1}, Lio/ktor/websocket/FrameCommonKt;->readReason(Lio/ktor/websocket/Frame$Close;)Lio/ktor/websocket/CloseReason;

    .line 197
    .line 198
    .line 199
    move-result-object p1

    .line 200
    invoke-static {p1}, Lkotlin/jvm/internal/Intrinsics;->checkNotNull(Ljava/lang/Object;)V

    .line 201
    .line 202
    .line 203
    # invokes: Lio/ktor/client/engine/okhttp/OkHttpWebsocketSessionKt;->a(Lio/ktor/websocket/CloseReason;)Z
    invoke-static {p1}, Lio/ktor/client/engine/okhttp/OkHttpWebsocketSessionKt;->access$isReserved(Lio/ktor/websocket/CloseReason;)Z

    .line 204
    .line 205
    .line 206
    move-result v0

    .line 207
    if-nez v0, :cond_d6

    .line 208
    .line 209
    move-object v4, p1

    .line 210
    :cond_d6
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end_d8
    .catchall {:try_start_6b .. :try_end_d8} :catchall_22

    .line 211
    .line 212
    :try_start_d8
    invoke-virtual {v4}, Lio/ktor/websocket/CloseReason;->getCode()S

    .line 213
    .line 214
    .line 215
    move-result v0

    .line 216
    invoke-virtual {v4}, Lio/ktor/websocket/CloseReason;->getMessage()Ljava/lang/String;

    .line 217
    .line 218
    .line 219
    move-result-object v1

    .line 220
    invoke-interface {v5, v0, v1}, Lokhttp3/WebSocket;->close(ILjava/lang/String;)Z
    :try_end_e3
    .catchall {:try_start_d8 .. :try_end_e3} :catchall_e4

    .line 221
    .line 222
    .line 223
    return-object p1

    .line 224
    :catchall_e4
    move-exception p1

    .line 225
    invoke-interface {v5}, Lokhttp3/WebSocket;->cancel()V

    .line 226
    .line 227
    .line 228
    throw p1

    .line 229
    :cond_e9
    :try_start_e9
    new-instance v0, Lio/ktor/client/engine/okhttp/UnsupportedFrameTypeException;

    .line 230
    .line 231
    invoke-direct {v0, p1}, Lio/ktor/client/engine/okhttp/UnsupportedFrameTypeException;-><init>(Lio/ktor/websocket/Frame;)V

    .line 232
    .line 233
    .line 234
    throw v0
    :try_end_ef
    .catchall {:try_start_e9 .. :try_end_ef} :catchall_22

    .line 235
    :cond_ef
    :try_start_ef
    invoke-virtual {v4}, Lio/ktor/websocket/CloseReason;->getCode()S

    .line 236
    .line 237
    .line 238
    move-result p1

    .line 239
    invoke-virtual {v4}, Lio/ktor/websocket/CloseReason;->getMessage()Ljava/lang/String;

    .line 240
    .line 241
    .line 242
    move-result-object v0

    .line 243
    invoke-interface {v5, p1, v0}, Lokhttp3/WebSocket;->close(ILjava/lang/String;)Z
    :try_end_fa
    .catchall {:try_start_ef .. :try_end_fa} :catchall_fd

    .line 244
    .line 245
    .line 246
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    .line 247
    .line 248
    return-object p1

    .line 249
    :catchall_fd
    move-exception p1

    .line 250
    invoke-interface {v5}, Lokhttp3/WebSocket;->cancel()V

    .line 251
    .line 252
    .line 253
    throw p1

    .line 254
    :goto_102
    :try_start_102
    invoke-virtual {v4}, Lio/ktor/websocket/CloseReason;->getCode()S

    .line 255
    .line 256
    .line 257
    move-result v0

    .line 258
    invoke-virtual {v4}, Lio/ktor/websocket/CloseReason;->getMessage()Ljava/lang/String;

    .line 259
    .line 260
    .line 261
    move-result-object v1

    .line 262
    invoke-interface {v5, v0, v1}, Lokhttp3/WebSocket;->close(ILjava/lang/String;)Z
    :try_end_10d
    .catchall {:try_start_102 .. :try_end_10d} :catchall_10e

    .line 263
    .line 264
    .line 265
    throw p1

    .line 266
    :catchall_10e
    move-exception p1

    .line 267
    invoke-interface {v5}, Lokhttp3/WebSocket;->cancel()V

    .line 268
    .line 269
    .line 270
    throw p1
.end method
