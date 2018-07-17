import pandas as pd

dataframe = pd.read_csv('tempdata.csv', sep="|", encoding = 'utf8', header='infer');

print(dataframe)
