from pyspark.context import SparkContext
from pyspark.sql.session import SparkSession
from pyspark.ml.evaluation import RegressionEvaluator
from pyspark.ml.recommendation import ALS, ALSModel
from pyspark.sql import Row

spark = SparkSession.builder.appName("ML").getOrCreate()

sc = spark.sparkContext

lines = spark.read.option("inferSchema", "true").option("header","true").option("delimiter","\t").csv("newdata.csv")

als = ALS(maxIter=10, rank=3, seed=10, nonnegative=True, regParam=1, userCol="kakao_id",itemCol="musicID",ratingCol="rating", coldStartStrategy="drop", implicitPrefs=False)
model = als.fit(lines)

model.write().overwrite().save("model")