from pyspark.context import SparkContext
from pyspark.sql.session import SparkSession
from pyspark.ml.evaluation import RegressionEvaluator
from pyspark.ml.recommendation import ALS
from pyspark.sql import Row

sc = SparkContext
spark = SparkSession.builder.appName("ML").getOrCreate()

lines = spark.read.option("inferSchema", "true").option("header","true").option("delimiter","|").csv("data.csv")

als = ALS(maxIter=5, regParam =0.01,userCol="kakao_id",itemCol="musicID",ratingCol="rating")
model = als.fit(lines)
predictions = model.transform(lines)

userRecs = model.recommendForAllUsers(10)
userRecs.show()