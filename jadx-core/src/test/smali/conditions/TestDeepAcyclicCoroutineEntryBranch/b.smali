.class public final Lo4/b;
.super Lvc/i;
.source "TestDeepAcyclicCoroutineEntryBranchExact.smali"

# interfaces
.implements Ldd/c;


# instance fields
.field public final synthetic s:I

.field public t:Li4/x;

.field public u:I

.field public synthetic v:Ljava/lang/Object;

.field public final synthetic w:Z

.field public final synthetic x:Z

.field public final synthetic y:Li4/u;

.field public final synthetic z:Ldd/b;


# direct methods
.method public synthetic constructor <init>(ZZLi4/u;Ltc/c;Ldd/b;I)V
    .registers 7

    #@0
    iput p6, p0, Lo4/b;->s:I

    #@2
    iput-boolean p1, p0, Lo4/b;->w:Z

    #@4
    iput-boolean p2, p0, Lo4/b;->x:Z

    #@6
    iput-object p3, p0, Lo4/b;->y:Li4/u;

    #@8
    iput-object p5, p0, Lo4/b;->z:Ldd/b;

    #@a
    const/4 p1, 0x2

    #@b
    invoke-direct {p0, p1, p4}, Lvc/i;-><init>(ILtc/c;)V

    #@e
    return-void
.end method


# virtual methods
.method public final k(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 4

    #@0
    iget v0, p0, Lo4/b;->s:I

    #@2
    check-cast p1, Li4/y;

    #@4
    check-cast p2, Ltc/c;

    #@6
    packed-switch v0, :pswitch_data_24

    #@9
    invoke-virtual {p0, p1, p2}, Lo4/b;->w(Ljava/lang/Object;Ltc/c;)Ltc/c;

    #@c
    move-result-object p1

    #@d
    check-cast p1, Lo4/b;

    #@f
    sget-object p2, Lpc/a0;->a:Lpc/a0;

    #@11
    invoke-virtual {p1, p2}, Lo4/b;->y(Ljava/lang/Object;)Ljava/lang/Object;

    #@14
    move-result-object p1

    #@15
    return-object p1

    #@16
    :pswitch_16
    invoke-virtual {p0, p1, p2}, Lo4/b;->w(Ljava/lang/Object;Ltc/c;)Ltc/c;

    #@19
    move-result-object p1

    #@1a
    check-cast p1, Lo4/b;

    #@1c
    sget-object p2, Lpc/a0;->a:Lpc/a0;

    #@1e
    invoke-virtual {p1, p2}, Lo4/b;->y(Ljava/lang/Object;)Ljava/lang/Object;

    #@21
    move-result-object p1

    #@22
    return-object p1

    #@23
    nop

    #@24
    :pswitch_data_24
    .packed-switch 0x0
        :pswitch_16
    .end packed-switch
.end method

.method public final w(Ljava/lang/Object;Ltc/c;)Ltc/c;
    .registers 12

    #@0
    iget v0, p0, Lo4/b;->s:I

    #@2
    packed-switch v0, :pswitch_data_2a

    #@5
    new-instance v1, Lo4/b;

    #@7
    iget-object v6, p0, Lo4/b;->z:Ldd/b;

    #@9
    const/4 v7, 0x1

    #@a
    iget-boolean v2, p0, Lo4/b;->w:Z

    #@c
    iget-boolean v3, p0, Lo4/b;->x:Z

    #@e
    iget-object v4, p0, Lo4/b;->y:Li4/u;

    #@10
    move-object v5, p2

    #@11
    invoke-direct/range {v1 .. v7}, Lo4/b;-><init>(ZZLi4/u;Ltc/c;Ldd/b;I)V

    #@14
    iput-object p1, v1, Lo4/b;->v:Ljava/lang/Object;

    #@16
    return-object v1

    #@17
    :pswitch_17
    move-object v5, p2

    #@18
    new-instance v2, Lo4/b;

    #@1a
    iget-object v7, p0, Lo4/b;->z:Ldd/b;

    #@1c
    const/4 v8, 0x0

    #@1d
    iget-boolean v3, p0, Lo4/b;->w:Z

    #@1f
    iget-boolean v4, p0, Lo4/b;->x:Z

    #@21
    move-object v6, v5

    #@22
    iget-object v5, p0, Lo4/b;->y:Li4/u;

    #@24
    invoke-direct/range {v2 .. v8}, Lo4/b;-><init>(ZZLi4/u;Ltc/c;Ldd/b;I)V

    #@27
    iput-object p1, v2, Lo4/b;->v:Ljava/lang/Object;

    #@29
    return-object v2

    #@2a
    :pswitch_data_2a
    .packed-switch 0x0
        :pswitch_17
    .end packed-switch
.end method

.method public final y(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 13

    #@0
    iget v0, p0, Lo4/b;->s:I

    #@2
    packed-switch v0, :pswitch_data_1b4

    #@5
    sget-object v0, Luc/a;->o:Luc/a;

    #@7
    iget v1, p0, Lo4/b;->u:I

    #@9
    iget-object v2, p0, Lo4/b;->z:Ldd/b;

    #@b
    iget-object v3, p0, Lo4/b;->y:Li4/u;

    #@d
    iget-boolean v4, p0, Lo4/b;->x:Z

    #@f
    const/4 v5, 0x4

    #@10
    const/4 v6, 0x3

    #@11
    const/4 v7, 0x2

    #@12
    const/4 v8, 0x1

    #@13
    if-eqz v1, :cond_49

    #@15
    if-eq v1, v8, :cond_3f

    #@17
    if-eq v1, v7, :cond_35

    #@19
    if-eq v1, v6, :cond_2c

    #@1b
    if-ne v1, v5, :cond_24

    #@1d
    iget-object v0, p0, Lo4/b;->v:Ljava/lang/Object;

    #@1f
    invoke-static {p1}, Lqk/e;->b0(Ljava/lang/Object;)V

    #@22
    goto/16 :goto_b4

    #@24
    :cond_24
    new-instance p1, Ljava/lang/IllegalStateException;

    #@26
    const-string v0, "call to \'resume\' before \'invoke\' with coroutine"

    #@28
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    #@2b
    throw p1

    #@2c
    :cond_2c
    iget-object v1, p0, Lo4/b;->v:Ljava/lang/Object;

    #@2e
    check-cast v1, Li4/y;

    #@30
    invoke-static {p1}, Lqk/e;->b0(Ljava/lang/Object;)V

    #@33
    goto/16 :goto_a5

    #@35
    :cond_35
    iget-object v1, p0, Lo4/b;->t:Li4/x;

    #@37
    iget-object v7, p0, Lo4/b;->v:Ljava/lang/Object;

    #@39
    check-cast v7, Li4/y;

    #@3b
    invoke-static {p1}, Lqk/e;->b0(Ljava/lang/Object;)V

    #@3e
    goto :goto_88

    #@3f
    :cond_3f
    iget-object v1, p0, Lo4/b;->t:Li4/x;

    #@41
    iget-object v8, p0, Lo4/b;->v:Ljava/lang/Object;

    #@43
    check-cast v8, Li4/y;

    #@45
    invoke-static {p1}, Lqk/e;->b0(Ljava/lang/Object;)V

    #@48
    goto :goto_6e

    #@49
    :cond_49
    invoke-static {p1}, Lqk/e;->b0(Ljava/lang/Object;)V

    #@4c
    iget-object p1, p0, Lo4/b;->v:Ljava/lang/Object;

    #@4e
    check-cast p1, Li4/y;

    #@50
    iget-boolean v1, p0, Lo4/b;->w:Z

    #@52
    if-eqz v1, :cond_cc

    #@54
    if-eqz v4, :cond_59

    #@56
    sget-object v1, Li4/x;->o:Li4/x;

    #@58
    goto :goto_5b

    #@59
    :cond_59
    sget-object v1, Li4/x;->p:Li4/x;

    #@5b
    :goto_5b
    if-nez v4, :cond_8e

    #@5d
    iput-object p1, p0, Lo4/b;->v:Ljava/lang/Object;

    #@5f
    iput-object v1, p0, Lo4/b;->t:Li4/x;

    #@61
    iput v8, p0, Lo4/b;->u:I

    #@63
    invoke-interface {p1, p0}, Li4/y;->d(Lvc/i;)Ljava/lang/Object;

    #@66
    move-result-object v8

    #@67
    if-ne v8, v0, :cond_6b

    #@69
    goto/16 :goto_db

    #@6b
    :cond_6b
    move-object v10, v8

    #@6c
    move-object v8, p1

    #@6d
    move-object p1, v10

    #@6e
    :goto_6e
    check-cast p1, Ljava/lang/Boolean;

    #@70
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z

    #@73
    move-result p1

    #@74
    if-nez p1, :cond_8b

    #@76
    invoke-virtual {v3}, Li4/u;->f()Li4/g;

    #@79
    move-result-object p1

    #@7a
    iput-object v8, p0, Lo4/b;->v:Ljava/lang/Object;

    #@7c
    iput-object v1, p0, Lo4/b;->t:Li4/x;

    #@7e
    iput v7, p0, Lo4/b;->u:I

    #@80
    invoke-virtual {p1, p0}, Li4/g;->a(Lvc/i;)Ljava/lang/Object;

    #@83
    move-result-object p1

    #@84
    if-ne p1, v0, :cond_87

    #@86
    goto :goto_db

    #@87
    :cond_87
    move-object v7, v8

    #@88
    :goto_88
    move-object p1, v1

    #@89
    move-object v1, v7

    #@8a
    goto :goto_91

    #@8b
    :cond_8b
    move-object p1, v1

    #@8c
    move-object v1, v8

    #@8d
    goto :goto_91

    #@8e
    :cond_8e
    move-object v10, v1

    #@8f
    move-object v1, p1

    #@90
    move-object p1, v10

    #@91
    :goto_91
    new-instance v7, Lo4/a;

    #@93
    const/4 v8, 0x1

    #@94
    const/4 v9, 0x0

    #@95
    invoke-direct {v7, v9, v2, v8}, Lo4/a;-><init>(Ltc/c;Ldd/b;I)V

    #@98
    iput-object v1, p0, Lo4/b;->v:Ljava/lang/Object;

    #@9a
    iput-object v9, p0, Lo4/b;->t:Li4/x;

    #@9c
    iput v6, p0, Lo4/b;->u:I

    #@9e
    invoke-interface {v1, p1, v7, p0}, Li4/y;->b(Li4/x;Ldd/c;Lvc/i;)Ljava/lang/Object;

    #@a1
    move-result-object p1

    #@a2
    if-ne p1, v0, :cond_a5

    #@a4
    goto :goto_db

    #@a5
    :cond_a5
    :goto_a5
    if-nez v4, :cond_ca

    #@a7
    iput-object p1, p0, Lo4/b;->v:Ljava/lang/Object;

    #@a9
    iput v5, p0, Lo4/b;->u:I

    #@ab
    invoke-interface {v1, p0}, Li4/y;->d(Lvc/i;)Ljava/lang/Object;

    #@ae
    move-result-object v1

    #@af
    if-ne v1, v0, :cond_b2

    #@b1
    goto :goto_db

    #@b2
    :cond_b2
    move-object v0, p1

    #@b3
    move-object p1, v1

    #@b4
    :goto_b4
    check-cast p1, Ljava/lang/Boolean;

    #@b6
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z

    #@b9
    move-result p1

    #@ba
    if-nez p1, :cond_db

    #@bc
    invoke-virtual {v3}, Li4/u;->f()Li4/g;

    #@bf
    move-result-object p1

    #@c0
    iget-object v1, p1, Li4/g;->b:Li4/i0;

    #@c2
    iget-object v2, p1, Li4/g;->e:Li4/f;

    #@c4
    iget-object p1, p1, Li4/g;->f:Li4/f;

    #@c6
    invoke-virtual {v1, v2, p1}, Li4/i0;->e(Ldd/a;Ldd/a;)V

    #@c9
    goto :goto_db

    #@ca
    :cond_ca
    move-object v0, p1

    #@cb
    goto :goto_db

    #@cc
    :cond_cc
    const-string v0, "null cannot be cast to non-null type androidx.room.coroutines.RawConnectionAccessor"

    #@ce
    invoke-static {p1, v0}, Led/n;->d(Ljava/lang/Object;Ljava/lang/String;)V

    #@d1
    check-cast p1, Lk4/t;

    #@d3
    invoke-interface {p1}, Lk4/t;->c()Lr4/a;

    #@d6
    move-result-object p1

    #@d7
    invoke-interface {v2, p1}, Ldd/b;->invoke(Ljava/lang/Object;)Ljava/lang/Object;

    #@da
    move-result-object v0

    #@db
    :cond_db
    :goto_db
    return-object v0

    #@dc
    :pswitch_dc
    sget-object v0, Luc/a;->o:Luc/a;

    #@de
    iget v1, p0, Lo4/b;->u:I

    #@e0
    iget-object v2, p0, Lo4/b;->z:Ldd/b;

    #@e2
    iget-object v3, p0, Lo4/b;->y:Li4/u;

    #@e4
    iget-boolean v4, p0, Lo4/b;->x:Z

    #@e6
    const/4 v5, 0x4

    #@e7
    const/4 v6, 0x3

    #@e8
    const/4 v7, 0x2

    #@e9
    const/4 v8, 0x1

    #@ea
    if-eqz v1, :cond_120

    #@ec
    if-eq v1, v8, :cond_116

    #@ee
    if-eq v1, v7, :cond_10c

    #@f0
    if-eq v1, v6, :cond_103

    #@f2
    if-ne v1, v5, :cond_fb

    #@f4
    iget-object v0, p0, Lo4/b;->v:Ljava/lang/Object;

    #@f6
    invoke-static {p1}, Lqk/e;->b0(Ljava/lang/Object;)V

    #@f9
    goto/16 :goto_18b

    #@fb
    :cond_fb
    new-instance p1, Ljava/lang/IllegalStateException;

    #@fd
    const-string v0, "call to \'resume\' before \'invoke\' with coroutine"

    #@ff
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    #@102
    throw p1

    #@103
    :cond_103
    iget-object v1, p0, Lo4/b;->v:Ljava/lang/Object;

    #@105
    check-cast v1, Li4/y;

    #@107
    invoke-static {p1}, Lqk/e;->b0(Ljava/lang/Object;)V

    #@10a
    goto/16 :goto_17c

    #@10c
    :cond_10c
    iget-object v1, p0, Lo4/b;->t:Li4/x;

    #@10e
    iget-object v7, p0, Lo4/b;->v:Ljava/lang/Object;

    #@110
    check-cast v7, Li4/y;

    #@112
    invoke-static {p1}, Lqk/e;->b0(Ljava/lang/Object;)V

    #@115
    goto :goto_15f

    #@116
    :cond_116
    iget-object v1, p0, Lo4/b;->t:Li4/x;

    #@118
    iget-object v8, p0, Lo4/b;->v:Ljava/lang/Object;

    #@11a
    check-cast v8, Li4/y;

    #@11c
    invoke-static {p1}, Lqk/e;->b0(Ljava/lang/Object;)V

    #@11f
    goto :goto_145

    #@120
    :cond_120
    invoke-static {p1}, Lqk/e;->b0(Ljava/lang/Object;)V

    #@123
    iget-object p1, p0, Lo4/b;->v:Ljava/lang/Object;

    #@125
    check-cast p1, Li4/y;

    #@127
    iget-boolean v1, p0, Lo4/b;->w:Z

    #@129
    if-eqz v1, :cond_1a3

    #@12b
    if-eqz v4, :cond_130

    #@12d
    sget-object v1, Li4/x;->o:Li4/x;

    #@12f
    goto :goto_132

    #@130
    :cond_130
    sget-object v1, Li4/x;->p:Li4/x;

    #@132
    :goto_132
    if-nez v4, :cond_165

    #@134
    iput-object p1, p0, Lo4/b;->v:Ljava/lang/Object;

    #@136
    iput-object v1, p0, Lo4/b;->t:Li4/x;

    #@138
    iput v8, p0, Lo4/b;->u:I

    #@13a
    invoke-interface {p1, p0}, Li4/y;->d(Lvc/i;)Ljava/lang/Object;

    #@13d
    move-result-object v8

    #@13e
    if-ne v8, v0, :cond_142

    #@140
    goto/16 :goto_1b2

    #@142
    :cond_142
    move-object v10, v8

    #@143
    move-object v8, p1

    #@144
    move-object p1, v10

    #@145
    :goto_145
    check-cast p1, Ljava/lang/Boolean;

    #@147
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z

    #@14a
    move-result p1

    #@14b
    if-nez p1, :cond_162

    #@14d
    invoke-virtual {v3}, Li4/u;->f()Li4/g;

    #@150
    move-result-object p1

    #@151
    iput-object v8, p0, Lo4/b;->v:Ljava/lang/Object;

    #@153
    iput-object v1, p0, Lo4/b;->t:Li4/x;

    #@155
    iput v7, p0, Lo4/b;->u:I

    #@157
    invoke-virtual {p1, p0}, Li4/g;->a(Lvc/i;)Ljava/lang/Object;

    #@15a
    move-result-object p1

    #@15b
    if-ne p1, v0, :cond_15e

    #@15d
    goto :goto_1b2

    #@15e
    :cond_15e
    move-object v7, v8

    #@15f
    :goto_15f
    move-object p1, v1

    #@160
    move-object v1, v7

    #@161
    goto :goto_168

    #@162
    :cond_162
    move-object p1, v1

    #@163
    move-object v1, v8

    #@164
    goto :goto_168

    #@165
    :cond_165
    move-object v10, v1

    #@166
    move-object v1, p1

    #@167
    move-object p1, v10

    #@168
    :goto_168
    new-instance v7, Lo4/a;

    #@16a
    const/4 v8, 0x0

    #@16b
    const/4 v9, 0x0

    #@16c
    invoke-direct {v7, v9, v2, v8}, Lo4/a;-><init>(Ltc/c;Ldd/b;I)V

    #@16f
    iput-object v1, p0, Lo4/b;->v:Ljava/lang/Object;

    #@171
    iput-object v9, p0, Lo4/b;->t:Li4/x;

    #@173
    iput v6, p0, Lo4/b;->u:I

    #@175
    invoke-interface {v1, p1, v7, p0}, Li4/y;->b(Li4/x;Ldd/c;Lvc/i;)Ljava/lang/Object;

    #@178
    move-result-object p1

    #@179
    if-ne p1, v0, :cond_17c

    #@17b
    goto :goto_1b2

    #@17c
    :cond_17c
    :goto_17c
    if-nez v4, :cond_1a1

    #@17e
    iput-object p1, p0, Lo4/b;->v:Ljava/lang/Object;

    #@180
    iput v5, p0, Lo4/b;->u:I

    #@182
    invoke-interface {v1, p0}, Li4/y;->d(Lvc/i;)Ljava/lang/Object;

    #@185
    move-result-object v1

    #@186
    if-ne v1, v0, :cond_189

    #@188
    goto :goto_1b2

    #@189
    :cond_189
    move-object v0, p1

    #@18a
    move-object p1, v1

    #@18b
    :goto_18b
    check-cast p1, Ljava/lang/Boolean;

    #@18d
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z

    #@190
    move-result p1

    #@191
    if-nez p1, :cond_1b2

    #@193
    invoke-virtual {v3}, Li4/u;->f()Li4/g;

    #@196
    move-result-object p1

    #@197
    iget-object v1, p1, Li4/g;->b:Li4/i0;

    #@199
    iget-object v2, p1, Li4/g;->e:Li4/f;

    #@19b
    iget-object p1, p1, Li4/g;->f:Li4/f;

    #@19d
    invoke-virtual {v1, v2, p1}, Li4/i0;->e(Ldd/a;Ldd/a;)V

    #@1a0
    goto :goto_1b2

    #@1a1
    :cond_1a1
    move-object v0, p1

    #@1a2
    goto :goto_1b2

    #@1a3
    :cond_1a3
    const-string v0, "null cannot be cast to non-null type androidx.room.coroutines.RawConnectionAccessor"

    #@1a5
    invoke-static {p1, v0}, Led/n;->d(Ljava/lang/Object;Ljava/lang/String;)V

    #@1a8
    check-cast p1, Lk4/t;

    #@1aa
    invoke-interface {p1}, Lk4/t;->c()Lr4/a;

    #@1ad
    move-result-object p1

    #@1ae
    invoke-interface {v2, p1}, Ldd/b;->invoke(Ljava/lang/Object;)Ljava/lang/Object;

    #@1b1
    move-result-object v0

    #@1b2
    :cond_1b2
    :goto_1b2
    return-object v0

    #@1b3
    nop

    #@1b4
    :pswitch_data_1b4
    .packed-switch 0x0
        :pswitch_dc
    .end packed-switch
.end method
