from pyspark.context import SparkContext
from pyspark.sql.session import SparkSession
from pyspark.ml.evaluation import RegressionEvaluator
from pyspark.ml.recommendation import ALS
from pyspark.sql import Row

sc = SparkContext
spark = SparkSession.builder.appName("ML").getOrCreate()

lines = spark.read.option("inferSchema", "true").option("header","true").option("delimiter","\t").csv("newdata.csv")

(training, test) = lines.randomSplit([0.8,0.2])

als = ALS(maxIter=10, rank=3, seed=10, nonnegative=True, regParam=0.1, userCol="kakao_id",itemCol="musicID",ratingCol="rating", coldStartStrategy="drop", implicitPrefs=False)
model = als.fit(lines)

userRecs = model.recommendForAllUsers(10)

model.write().overwrite().save("model")
userRecs.toPandas().to_csv("userRecs.txt", index=False, sep="\t")