use geo-db
db.layers.drop()
layers = db.layers

layers.ensureIndex({'name': -1})
