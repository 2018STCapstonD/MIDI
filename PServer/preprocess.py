import pandas as pd
import os

f_path = os.getcwd()
#새로 받은 데이터 오픈
tempdf = pd.read_csv(f_path+'/tempdata.csv', sep = "\t", encoding = 'utf8', header='infer')

#기존 데이터 오픈
try:
    df = pd.read_csv(f_path+'/data.csv', sep = "\t", encoding = 'utf8', header='infer')
except FileNotFoundError :
    f = open(f_path+"/data.csv", 'a')
    f.write("kakao_id"+"\t"+"title"+"\t"+"album"+"\t"+"artist"+"\t"+"rating"+"\t"+"musicID")
    f.close()
    df = pd.read_csv(f_path+'/data.csv', sep = "\t", encoding = 'utf8', header='infer')

print(tempdf.tail)
tempdf.columns = ["kakao_id","title","album","artist","rating","musicID"]


for row in tempdf.iterrows():
    #한줄씩 실행
    #일치하는 ID가 있으면 rating만 갱신, 없으면 데이터 추가
    to_append = row[1].to_frame().T
    if df.loc[(df['musicID'] == row[1].musicID) & (df['kakao_id'] == row[1].kakao_id)].empty:
        df = df.append(to_append)
    else:
        df.loc[(df['musicID'] == row[1].musicID) & (df['kakao_id'] == row[1].kakao_id), 'rating'] = row[1].rating

#데이터 저장
df.to_csv(f_path+'/data.csv', sep = "\t", encoding = 'utf8', header='infer', index=False)
