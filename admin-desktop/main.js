const {app,BrowserWindow}=require('electron');
app.whenReady().then(()=>{const win=new BrowserWindow({width:1440,height:950});win.loadURL(process.env.COMMUTEMATE_ADMIN_URL||'http://localhost:4200');});
