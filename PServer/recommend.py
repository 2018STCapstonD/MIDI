from pyspark.context import SparkContext
from pyspark.sql.session import SparkSession
from pyspark.ml.recommendation import ALSModel
import pandas as pd

sc = SparkContext
spark = SparkSession.builder.appName("ML").getOrCreate()

model = ALSModel.load('model')

lines = spark.read.option("inferSchema", "true").option("header","true").option("delimiter","\t").csv("data2.csv")

predictions = model.transform(lines)

userSubsetRecs = model.recommendForUserSubset(lines, 10)
userSubsetRecs.show()