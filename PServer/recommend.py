from pyspark.context import SparkContext
from pyspark.sql.session import SparkSession
from pyspark.ml.recommendation import ALSModel
import pandas as pd
import os
from pyspark.sql import Row

f_path = os.getcwd()

spark = SparkSession.builder.appName("ML").getOrCreate()
sc = spark.sparkContext

musicdf = pd.read_csv('musicdata.csv', sep = "\t", encoding = 'utf8', header='infer')

model = ALSModel.load('model')

userRecs = model.recommendForAllUsers(10)

userRecs = userRecs.toPandas()

recs = pd.DataFrame(columns=['kakao_id', 'title', 'album', 'artist'])

for row in userRecs.iterrows():
    for i in range(10) :
        #kakao_id, title, album, artist
        kakao_id = row[1][0]
        title = musicdf.loc[(musicdf['musicID'] == row[1][1][i][0])].title.item()
        album = musicdf.loc[(musicdf['musicID'] == row[1][1][i][0])].album.item()
        artist = musicdf.loc[(musicdf['musicID'] == row[1][1][i][0])].artist.item()
        recs = recs.append({'kakao_id' : kakao_id,'title' :title, 'album' :album, 'artist' : artist}, ignore_index=True)

recs.to_csv('userRecs.csv', sep='\t', encoding = 'utf8', header=False, index=False)