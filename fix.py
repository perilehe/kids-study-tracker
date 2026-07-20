with open('app.html','r',encoding='utf-8') as f:
    c=f.read()

# The bug: setStatsView('WEEKLY') inside a JS single-quoted string
# Fix: change to setStatsView("WEEKLY")
c = c.replace("setStatsView('WEEKLY')", 'setStatsView("WEEKLY")')
c = c.replace("setStatsView('MONTHLY')", 'setStatsView("MONTHLY")')

with open('app.html','w',encoding='utf-8') as f:
    f.write(c)
print('Done')
