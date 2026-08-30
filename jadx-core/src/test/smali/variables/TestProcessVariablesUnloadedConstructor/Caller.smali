.class public Lvariables/pv/Caller;
.super Ljava/lang/Object;

.method public static make()Lvariables/pv/Target;
    .registers 2

    const/4 v0, 0x0

    new-instance v1, Lvariables/pv/Target;

    invoke-direct {v1, v0}, Lvariables/pv/Target;-><init>(Lvariables/pv/Marker;)V

    return-object v1
.end method
