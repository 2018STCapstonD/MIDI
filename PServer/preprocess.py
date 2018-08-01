import pandas as pd
import hashlib

#새로 받은 데이터 오픈
tempdf = pd.read_csv('tempdata.csv', sep = "|", encoding = 'utf8', header='infer')

#기존 데이터 오픈
try:
    df = pd.read_csv('data.csv', sep = "|", encoding = 'utf8', header='infer')
except FileNotFoundError :
    f = open("data.csv", 'a')
    f.write("kakao_id|title|album|artist|rating|musicID")
    f.close()
    df = pd.read_csv('data.csv', sep = "|", encoding = 'utf8', header='infer')

#제목+앨범명으로 해시값 생성해 추가.
#환경변수 PYTHONHASHSEED = 0 꼭 설정할것!!
to_hash = (tempdf["title"]+tempdf["album"])
tempdf["musicID"] = to_hash.apply(hash)
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
df.to_csv('data.csv', sep = "|", encoding = 'utf8', header='infer', index=False)