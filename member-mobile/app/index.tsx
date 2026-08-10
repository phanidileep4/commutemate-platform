import { useState } from 'react';
import { SafeAreaView, ScrollView, Text, TouchableOpacity, View } from 'react-native';

const intents=['Quiet','Social','Networking','Fastest','Max impact'];
const matches=[['Sam','94%','8:05 AM','Great route + repeat-ride fit'],['Alex','91%','8:10 AM','Best route + similar commute style'],['Jordan','84%','7:55 AM','Fastest route + parking impact']];
export default function Home(){
 const [intent,setIntent]=useState('Quiet');
 return <SafeAreaView style={{flex:1,backgroundColor:'#fff'}}><ScrollView contentContainerStyle={{padding:22}}>
  <Text style={{fontWeight:'800',fontSize:22}}>CommuteMate</Text><Text style={{color:'#667085',marginTop:4}}>Northstar Corp · West Campus</Text>
  <Text style={{marginTop:36,color:'#667085'}}>Monday commute</Text><Text style={{fontSize:34,fontWeight:'800',marginTop:6}}>How do you want to commute today?</Text>
  <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{marginVertical:20}}>{intents.map(i=><TouchableOpacity key={i} onPress={()=>setIntent(i)} style={{paddingVertical:10,paddingHorizontal:15,borderRadius:24,marginRight:8,backgroundColor:intent===i?'#111827':'#f2f4f7'}}><Text style={{color:intent===i?'white':'#111827',fontWeight:'600'}}>{i}</Text></TouchableOpacity>)}</ScrollView>
  <Text style={{fontSize:22,fontWeight:'800',marginBottom:12}}>Best matches</Text>{matches.map(m=><View key={m[0]} style={{borderWidth:1,borderColor:'#eaecf0',borderRadius:18,padding:18,marginBottom:12}}><View style={{flexDirection:'row',justifyContent:'space-between'}}><Text style={{fontSize:21,fontWeight:'800'}}>{m[0]}</Text><Text style={{fontWeight:'800'}}>{m[1]}</Text></View><Text style={{color:'#667085',marginVertical:8}}>{m[2]}</Text><Text>{m[3]}</Text><TouchableOpacity style={{backgroundColor:'#111827',padding:12,borderRadius:10,marginTop:14}}><Text style={{color:'white',textAlign:'center',fontWeight:'700'}}>View match</Text></TouchableOpacity></View>)}
  <Text style={{color:'#667085',marginTop:8}}>Exact pickup details are shared only after mutual acceptance.</Text>
 </ScrollView></SafeAreaView>
}
