import pandas as pd
import hashlib

dataframe = pd.read_csv('tempdata.csv', sep="|", encoding = 'utf8', header='infer')

to_hash = (dataframe["title"]+dataframe["album"])

dataframe["musicID"] = to_hash.apply(hash)

print(dataframe)
