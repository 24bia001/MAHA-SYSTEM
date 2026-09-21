import React, {useEffect, useMemo, useState} from "react";
import {createRoot} from "react-dom/client";
import "./styles.css";

const API = import.meta.env.VITE_API_URL || "http://localhost:8081/api";
const images = [
  "https://images.unsplash.com/photo-1600607687920-4e2a09cf159d?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600047509807-ba8f99d2cdde?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600566753086-00f18fb6b3ea?auto=format&fit=crop&w=1200&q=85"
];

async function api(path, options={}) {
  const token = localStorage.getItem("ehouse_token");
  const headers = {"Content-Type":"application/json", ...(options.headers||{})};
  if(token) headers.Authorization = `Bearer ${token}`;
  const res = await fetch(`${API}${path}`, {...options, headers});
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  if(!res.ok) throw new Error(data?.message || data?.error || text || `Request failed (${res.status})`);
  return data;
}

function statusOf(value){ return String(value||"").trim().toUpperCase(); }
function money(n){ return new Intl.NumberFormat("en-TZ",{style:"currency",currency:"TZS",maximumFractionDigits:0}).format(Number(n||0)); }
function initials(name="User"){ return name.split(" ").map(x=>x[0]).slice(0,2).join("").toUpperCase(); }
function readImage(file,setter){
  if(!file) return;
  if(file.size > 2 * 1024 * 1024){ alert("Please choose an image smaller than 2 MB."); return; }
  const reader=new FileReader();
  reader.onload=()=>setter(reader.result);
  reader.readAsDataURL(file);
}

function App(){
  const [page,setPage]=useState("home");
  const [houses,setHouses]=useState([]);
  const [loading,setLoading]=useState(true);
  const [query,setQuery]=useState("");
  const [selected,setSelected]=useState(null);
  const [auth,setAuth]=useState(()=>JSON.parse(localStorage.getItem("ehouse_user")||"null"));
  const [notice,setNotice]=useState("");
  const [authMode,setAuthMode]=useState("login");
  const [authRole,setAuthRole]=useState("customer");

  const loadHouses=async()=>{
    setLoading(true);
    try {
      const data = await api(auth?.role==="CUSTOMER" ? "/customer/houses" : "/houses");
      setHouses(Array.isArray(data)?data:[]);
    } catch(e) { setNotice(e.message); }
    finally { setLoading(false); }
  };
  useEffect(()=>{ if(page==="home" || page==="dashboard" || page==="seller") loadHouses(); },[page,auth?.role]);

  const filtered=useMemo(()=>houses.filter(h=>
    `${h.title} ${h.location} ${h.description}`.toLowerCase().includes(query.toLowerCase())
  ),[houses,query]);

  function logout(){
    localStorage.removeItem("ehouse_token"); localStorage.removeItem("ehouse_user");
    setAuth(null); setPage("home"); setNotice("You have been signed out.");
  }
  function go(p){setPage(p); window.scrollTo({top:0,behavior:"smooth"});}

  return <div className="app">
    <header className="nav">
      <button type="button" className="brand" onClick={()=>go("home")}><span className="brandMark">EH</span><span>E-House</span></button>
      <nav>
        {!auth && <>
          <button className={page==="home"?"active":""} onClick={()=>go("home")}>Explore</button>
          <button onClick={()=>go("about")}>How it works</button>
        </>}
        {auth?.role==="CUSTOMER" && <button onClick={()=>go("dashboard")}>My space</button>}
        {auth?.role==="SELLER" && <button onClick={()=>go("seller")}>Seller desk</button>}
        {auth?.role==="ADMIN" && <button onClick={()=>go("admin")}>Management</button>}
      </nav>
      <div className="navRight">
        {auth ? <button className="profileChip" onClick={()=>go(auth.role==="CUSTOMER"?"dashboard":auth.role==="SELLER"?"seller":"admin")}>
  {auth.image ? <img src={auth.image} alt="" /> : <span>{initials(auth.name)}</span>}{auth.name}
</button>
        : <button type="button" className="outlineBtn" onClick={()=>{setAuthMode("login");go("auth")}}>Sign in</button>}
        {!auth && <button className="primaryBtn" onClick={()=>{setAuthMode("register");setAuthRole("customer");go("auth")}}>Join E-House</button>}
        {auth && <button type="button" className="iconBtn" title="Sign out" onClick={logout}>↗</button>}
      </div>
    </header>

    {notice && <div className="toast"><span>●</span>{notice}<button onClick={()=>setNotice("")}>×</button></div>}

    {page==="home" && <Home houses={filtered} loading={loading} query={query} setQuery={setQuery} openHouse={h=>{setSelected(h);go("details")}} go={go}/>}
    {page==="details" && <Details house={selected} auth={auth} go={go} setNotice={setNotice}/>}
    {page==="about" && <About go={go}/>}
    {page==="auth" && <Auth mode={authMode} role={authRole} setRole={setAuthRole} setMode={setAuthMode} onAuth={u=>{setAuth(u);go(u.role==="CUSTOMER"?"dashboard":u.role==="SELLER"?"seller":"admin")}} setNotice={setNotice}/>}
    {page==="dashboard" && <CustomerDashboard auth={auth} houses={houses} go={go} setNotice={setNotice} setSelected={setSelected} logout={logout}/>}
    {page==="seller" && <SellerDesk auth={auth} go={go} setNotice={setNotice} logout={logout}/>}
    {page==="admin" && <AdminDesk setNotice={setNotice} go={go} logout={logout}/>}
    <Footer auth={auth} go={go}/>
  </div>
}

function Footer({auth,go}){
 return <footer className={`siteFooter ${auth?"dashboardFooter":""}`}>
   <div className="footerBottom">
     <span>© {new Date().getFullYear()} MAHA E-HOUSING. All rights reserved.</span>
     <span>MAHA E-HOUSING — digital property selling.</span>
   </div>
 </footer>
}

function Home({houses,loading,query,setQuery,openHouse,go}){
 return <main>
  <section className="hero">
    <div className="heroGlow"></div>
    <div className="heroCopy">
      <div className="eyebrow">REAL ESTATE, REIMAGINED</div>
      <h1>Find a place<br/><i>worth coming home to.</i></h1>
      <p>Discover verified homes, book a viewing and keep every step of your purchase in one calm, simple space.</p>
      <div className="searchBox"><span>⌕</span><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search by location, house name or feature..." /><kbd>⌘ K</kbd></div>
      <div className="heroStats"><span><b>01</b> Explore</span><span><b>02</b> Book</span><span><b>03</b> Own</span></div>
    </div>
    <div className="heroVisual"><img src={images[0]}/><div className="floatingCard"><span className="dot"></span><div><b>Curated homes</b><small>Made for real life</small></div><strong>→</strong></div></div>
  </section>

  <section className="section">
   <div className="sectionHead"><div><span className="eyebrow">THE COLLECTION</span><h2>Homes with a story.</h2></div><button type="button" className="textBtn" onClick={()=>setQuery("")}>Clear search ↗</button></div>
   {loading ? <div className="loader">Loading homes<span>•••</span></div> :
    houses.length ? <div className="grid">{houses.map((h,i)=><HouseCard key={h.houseId||i} h={h} i={i} onClick={()=>openHouse(h)}/>)}</div> :
    <div className="empty"><div>⌂</div><h3>No homes found</h3><p>Try another location or search phrase.</p></div>}
  </section>

  <section className="manifesto">
    <div><span className="eyebrow">WHY E-HOUSE</span><h2>Less paperwork.<br/><i>More certainty.</i></h2></div>
    <div className="principles"><article><b>01</b><h3>One place</h3><p>Bookings, payments, sales and documents stay connected to your property journey.</p></article><article><b>02</b><h3>Human support</h3><p>See seller details and keep the conversation close to the home you want.</p></article><article><b>03</b><h3>Clear progress</h3><p>Your dashboard turns a complicated purchase into simple next steps.</p></article></div>
  </section>
  <section className="cta"><div><span className="eyebrow">READY WHEN YOU ARE</span><h2>Your next address<br/>could be here.</h2></div><button className="lightBtn" onClick={()=>go("auth")}>Create your account →</button></section>
 </main>
}

function HouseCard({h,i,onClick}){
 return <article className="houseCard" onClick={onClick}>
   <div className="imageWrap"><img src={h.image||images[i%images.length]}/><span className="tag">AVAILABLE</span><button onClick={e=>{e.stopPropagation();onClick()}}>↗</button></div>
   <div className="cardBody"><div><span className="muted">{h.location}</span><h3>{h.title}</h3></div><strong>{money(h.price)}</strong></div>
   <div className="features"><span>⌂ {h.bedrooms} beds</span><span>◈ {h.bathrooms} baths</span><span>#{h.houseId}</span></div>
 </article>
}

function Details({house,auth,go,setNotice}){
 if(!house) return <div className="empty pagePad"><h2>Select a house first.</h2><button className="primaryBtn" onClick={()=>go("home")}>Back to homes</button></div>;
 const [date,setDate]=useState("");
 async function book(){
   if(!auth){go("auth");return}
   try { await api(auth.role==="CUSTOMER"?"/customer/bookings":"/bookings",{method:"POST",body:JSON.stringify(auth.role==="CUSTOMER"?{bookingDate:date,houseId:house.houseId}:{bookingDate:date,status:"PENDING",customerId:1,houseId:house.houseId})}); setNotice("Viewing request submitted successfully."); }
   catch(e){setNotice(e.message)}
 }
 return <main className="pagePad"><button type="button" className="backBtn" onClick={()=>go("home")}>← Back to collection</button>
  <div className="detail"><div className="detailImage"><img src={house.image||images[(house.houseId||1)%images.length]}/><span>HOUSE #{house.houseId}</span></div>
   <div className="detailInfo"><span className="eyebrow">{house.location}</span><h1>{house.title}</h1><div className="bigPrice">{money(house.price)}</div><div className="featureRow"><b>{house.bedrooms}<small>Bedrooms</small></b><b>{house.bathrooms}<small>Bathrooms</small></b><b>01<small>Listing</small></b></div><p>{house.description}</p><div className="bookingPanel"><div><b>Book a private viewing</b><small>Choose a date that works for you.</small></div><input type="date" value={date} onChange={e=>setDate(e.target.value)}/><button className="primaryBtn" disabled={!date} onClick={book}>Request viewing →</button></div></div>
  </div>
 </main>
}

function About({go}){return <main className="pagePad about"><span className="eyebrow">HOW E-HOUSE WORKS</span><h1>A quieter way to<br/><i>buy property.</i></h1><div className="steps"><div><b>01</b><h2>Discover</h2><p>Browse the available homes coming from your E-House backend.</p></div><div><b>02</b><h2>Connect</h2><p>Create an account and request a viewing for the home you love.</p></div><div><b>03</b><h2>Move forward</h2><p>Track bookings, payments, sales and documents from your portal.</p></div></div><button className="primaryBtn" onClick={()=>go("home")}>Explore homes →</button></main>}

function Auth({mode,role,setRole,setMode,onAuth,setNotice}){
 const [form,setForm]=useState({name:"",email:"",phone:"",address:"",nida:"",password:""});
 const [busy,setBusy]=useState(false);

 async function submit(e){
   e.preventDefault();
   setBusy(true);
   try{
     if(mode==="register"){
       if(role!=="customer") throw new Error("Only customers can register themselves. Please contact the administrator for a seller account.");
       const data=await api("/auth/customer/register",{method:"POST",body:JSON.stringify({
         name:form.name,email:form.email,phone:form.phone,address:form.address,nida:form.nida,password:form.password,image:form.image||null
       })});
       setNotice("Customer account created. Please sign in.");
       setMode("login");
       return;
     }

     const endpoint=role==="customer"
       ? "/auth/customer/login"
       : role==="seller"
         ? "/auth/seller/login"
         : "/auth/management/login";

     const data=await api(endpoint,{method:"POST",body:JSON.stringify({
       email:form.email,password:form.password
     })});

     const r=role==="customer"?"CUSTOMER":role==="seller"?"SELLER":"ADMIN";
     const user={...data,role:r};
     localStorage.setItem("ehouse_token",data.token);
     localStorage.setItem("ehouse_user",JSON.stringify(user));
     onAuth(user);
   }catch(e){setNotice(e.message)}
   finally{setBusy(false)}
 }

 return <main className="authPage">
   <div className="authVisual"><span className="brandMark">EH</span><h1>Property should feel<br/><i>personal.</i></h1><p>A focused portal for finding, booking and owning your next home.</p></div>
   <div className="authPanel">
     <span className="eyebrow">{mode==="login"?"WELCOME BACK":"CUSTOMER REGISTRATION"}</span>
     <h2>{mode==="login"?"Sign in":"Create customer account"}</h2>

     {mode==="login" ? <div className="roleTabs">
       {["customer","seller","admin"].map(r=><button key={r} className={role===r?"selected":""} onClick={()=>setRole(r)}>{r}</button>)}
     </div> : <div className="noticeBox">Customer registration is open. Seller accounts are created by an administrator.</div>}

     <form onSubmit={submit}>
       {mode==="register" && <><input placeholder="Full name" required value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/>
         <div className="two"><input placeholder="Phone" required value={form.phone} onChange={e=>setForm({...form,phone:e.target.value})}/><input placeholder="NIDA" required value={form.nida} onChange={e=>setForm({...form,nida:e.target.value})}/></div><input placeholder="Address" required value={form.address} onChange={e=>setForm({...form,address:e.target.value})}/>
         <label className="fileField">Profile picture<input type="file" accept="image/*" onChange={e=>readImage(e.target.files?.[0],image=>setForm({...form,image}))}/></label>
       </>}
       <input type="email" placeholder="Email address" required value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/>
       <input type="password" placeholder="Password" minLength="6" required value={form.password} onChange={e=>setForm({...form,password:e.target.value})}/>
       <button className="primaryBtn wide" disabled={busy}>{busy?"Please wait…":mode==="login"?"Sign in →":"Create account →"}</button>
     </form>
     <p className="switch">{mode==="login"?"New customer?":"Already have an account?"} <button onClick={()=>{setMode(mode==="login"?"register":"login");setRole("customer")}}>{mode==="login"?"Register":"Sign in"}</button></p>
   </div>
 </main>
}


function DashboardSidebar({role, name, image, tab, setTab, items, go, logout, actionLabel, onAction}){
 return <aside className="luxSidebar">
   <div className="sideBrand"><span className="brandMark">EH</span><div><b>MAHA E-HOUSING</b><small>{role} PORTAL</small></div></div>
   <div className="sideUser">
     <div className="sideAvatar">{image?<img src={image} alt=""/>:initials(name)}</div>
     <div><b>{name||role}</b><small>Signed in</small></div>
   </div>
   <div className="sideLabel">WORKSPACE</div>
   <nav className="sideNav">{items.map(item=><button key={item.id} className={tab===item.id?"active":""} onClick={()=>setTab(item.id)}><span>{item.icon}</span><span>{item.label}</span>{tab===item.id&&<i>•</i>}</button>)}</nav>
   <div className="sideBottom">
     {actionLabel&&<button className="sideAction" onClick={onAction}>{actionLabel} <span>↗</span></button>}
     <button className="sideHome" onClick={logout}>↪ <span>Logout</span></button>
   </div>
 </aside>
}

function CustomerDashboard({auth,houses,go,setNotice,setSelected,logout}){
 const [tab,setTab]=useState("overview");
 const [bookings,setBookings]=useState([]), [payments,setPayments]=useState([]), [sales,setSales]=useState([]), [docs,setDocs]=useState([]);
 const [pay,setPay]=useState({bookingId:"",amount:"",paymentMethod:"Mobile Money"});
 const load=async()=>{
  const results=await Promise.allSettled([api("/customer/bookings"),api("/customer/payments"),api("/customer/sales"),api("/customer/documents")]);
  const setters=[setBookings,setPayments,setSales,setDocs];
  results.forEach((r,i)=>{ if(r.status==="fulfilled") setters[i](Array.isArray(r.value)?r.value:[]); else { setters[i]([]); setNotice(r.reason?.message||"Could not load customer data."); } });
};
 useEffect(()=>{load()},[]);
 async function cancel(id){try{await api(`/customer/bookings/${id}/cancel`,{method:"PUT"});setNotice("Booking cancelled.");load()}catch(e){setNotice(e.message)}}
 async function payBooking(e){e.preventDefault();try{await api("/customer/payments",{method:"POST",body:JSON.stringify({bookingId:Number(pay.bookingId),amount:Number(pay.amount),paymentDate:new Date().toISOString().slice(0,10),paymentMethod:pay.paymentMethod,status:"PAID"})});setNotice("Payment received. Your receipt is now available below.");setPay({bookingId:"",amount:"",paymentMethod:"Mobile Money"});load()}catch(e){setNotice(e.message)}}
 function receipt(p){const w=window.open("","_blank","width=800,height=700"); if(!w)return; w.document.write(`<html><head><title>E-House Receipt #${p.paymentId}</title><style>body{font-family:Arial;padding:40px}h1{margin-bottom:4px}.line{border-bottom:1px solid #ddd;padding:12px 0}</style></head><body><h1>MAHA E-HOUSING</h1><p>Official Payment Receipt</p><div class="line">Receipt #: ${p.paymentId}</div><div class="line">Booking #: ${p.bookingId}</div><div class="line">Date: ${p.paymentDate}</div><div class="line">Method: ${p.paymentMethod}</div><div class="line"><b>Amount: ${money(p.amount)}</b></div><div class="line">Status: ${p.status}</div><p>Thank you for using MAHA E-HOUSING.</p><script>window.print()</script></body></html>`);w.document.close()}
 return <main className="dashboardShell"><DashboardSidebar role="CUSTOMER" image={auth?.image} name={auth?.name} tab={tab} setTab={setTab} go={go} logout={logout} actionLabel="Browse houses" onAction={()=>go("home")} items={[{id:"overview",label:"Overview",icon:"◈"},{id:"houses",label:"Discover houses",icon:"⌂"},{id:"bookings",label:"My bookings",icon:"◷"},{id:"payments",label:"Payments & receipts",icon:"◇"},{id:"sales",label:"Purchased homes",icon:"✓"},{id:"documents",label:"Documents",icon:"▣"}]}/><section className="dashboardMain pagePad"><div className="dashTop"><div><span className="eyebrow">CUSTOMER PORTAL</span><h1>Hello, {auth?.name?.split(" ")[0]||"there"}.</h1><p>Homes, bookings, payments, receipts, purchases and documents.</p></div><button className="primaryBtn" onClick={()=>go("home")}>Browse houses →</button></div>
 <div className="dashContent">{tab==="overview"&&<><div className="metricRow"><div><small>Available houses</small><b>{houses.length}</b></div><div><small>Bookings</small><b>{bookings.length}</b></div><div><small>Payments</small><b>{payments.length}</b></div><div><small>Purchases</small><b>{sales.length}</b></div></div><div className="dashCard"><h3>Next steps</h3><p className="muted">Browse a house, request a viewing, wait for seller confirmation, then pay and keep your receipt here.</p></div></>}
 {tab==="houses"&&<div className="grid">{houses.map((h,i)=><HouseCard key={h.houseId} h={h} i={i} onClick={()=>{setSelected(h);go("details")}}/>)}</div>}
 {tab==="bookings"&&<DataList title="My bookings" data={bookings} cancel={cancel}/>} 
 {tab==="payments"&&<><div className="dashCard"><h3>Make a payment</h3><p className="muted">Payment becomes active only after the seller approves your booking. Approved bookings are available below.</p>{!bookings.some(b=>statusOf(b.status)==="CONFIRMED"&&!payments.some(p=>p.bookingId===b.bookingId&&statusOf(p.status)==="PAID"))&&<div className="paymentLocked">No approved booking is ready for payment yet.</div>}<form className="formCard inlineForm" onSubmit={payBooking}><select required value={pay.bookingId} onChange={e=>setPay({...pay,bookingId:e.target.value})}><option value="">Select approved booking</option>{bookings.filter(b=>statusOf(b.status)==="CONFIRMED"&&!payments.some(p=>p.bookingId===b.bookingId&&statusOf(p.status)==="PAID")).map(b=><option key={b.bookingId} value={b.bookingId}>#{b.bookingId} — {b.houseTitle} — {b.bookingDate}</option>)}</select><input type="number" min="1" required placeholder="Amount (TZS)" value={pay.amount} onChange={e=>setPay({...pay,amount:e.target.value})}/><select value={pay.paymentMethod} onChange={e=>setPay({...pay,paymentMethod:e.target.value})}><option>Mobile Money</option><option>Bank Transfer</option><option>Cash</option></select><button className="primaryBtn" disabled={!bookings.some(b=>statusOf(b.status)==="CONFIRMED"&&!payments.some(p=>p.bookingId===b.bookingId&&statusOf(p.status)==="PAID"))}>Pay now →</button></form></div><DataList title="Payment history & receipts" data={payments} receipt={receipt}/></>}
 {tab==="sales"&&<DataList title="Purchased homes" data={sales}/>} {tab==="documents"&&<DataList title="My documents" data={docs}/>}</div></section></main>
}

function DataList({title,data,cancel,receipt}){return <div className="dashCard"><h3>{title}</h3>{data.length?data.map(x=>{const raw=x.status||x.bookingStatus||x.documentStatus||"RECORDED";const label=raw==="CONFIRMED"?"APPROVED":raw;return <div className="listRow" key={x.bookingId||x.paymentId||x.saleId||x.documentId}><span>#{x.bookingId||x.paymentId||x.saleId||x.documentId}</span><b>{x.houseTitle||x.documentType||x.amount&&money(x.amount)||"Record"}</b><span>{x.bookingDate||x.paymentDate||x.saleDate||""}</span><em className={`status ${String(raw).toLowerCase()}`}>{label}</em>{cancel&&raw!=="CANCELLED"&&<button className="smallBtn" onClick={()=>cancel(x.bookingId)}>Cancel</button>}{receipt&&<button className="smallBtn" onClick={()=>receipt(x)}>Receipt</button>}</div>}) : <p className="muted">Nothing here yet.</p>}</div>}

function SellerDesk({auth,go,setNotice,logout}){
 const emptyHouse={title:"",location:"",description:"",price:"",bedrooms:"",bathrooms:"",image:""};
 const [tab,setTab]=useState("overview"),[houses,setHouses]=useState([]),[bookings,setBookings]=useState([]),[payments,setPayments]=useState([]),[sales,setSales]=useState([]);
 const [form,setForm]=useState(emptyHouse),[editing,setEditing]=useState(null),[showAdd,setShowAdd]=useState(false),[pay,setPay]=useState({bookingId:"",amount:"",paymentMethod:"Bank Transfer"});
 const load=async()=>{
  const results=await Promise.allSettled([api("/houses"),api("/bookings"),api("/payments"),api("/sales")]);
  const setters=[setHouses,setBookings,setPayments,setSales];
  const labels=["houses","bookings","payments","sales"];
  results.forEach((r,i)=>{
    if(r.status==="fulfilled") setters[i](Array.isArray(r.value)?r.value:[]);
    else { setters[i]([]); setNotice(r.reason?.message||`Could not load ${labels[i]}.`); }
  });
};
 useEffect(()=>{load()},[]);
 function openAdd(){setEditing(null);setForm(emptyHouse);setShowAdd(true)}
 function openEdit(h){setEditing(h.houseId);setForm({...h,price:h.price,bedrooms:h.bedrooms,bathrooms:h.bathrooms});setShowAdd(true)}
 async function save(e){e.preventDefault();try{const body={...form,price:Number(form.price),bedrooms:Number(form.bedrooms),bathrooms:Number(form.bathrooms)};if(editing)await api(`/houses/${editing}`,{method:"PUT",body:JSON.stringify(body)});else await api("/houses",{method:"POST",body:JSON.stringify(body)});setNotice(editing?"House updated successfully.":"House published and is now visible to customers.");setShowAdd(false);setEditing(null);setForm(emptyHouse);load()}catch(e){setNotice(e.message)}}
 async function del(id){if(!confirm("Delete this house?"))return;try{await api(`/houses/${id}`,{method:"DELETE"});setNotice("House deleted.");load()}catch(e){setNotice(e.message)}}
 async function confirmBooking(id){try{await api(`/bookings/${id}/confirm`,{method:"PUT"});setNotice("Booking approved.");load()}catch(e){setNotice(e.message)}}
 async function cancelBooking(id){try{await api(`/bookings/${id}/cancel`,{method:"PUT"});setNotice("Booking cancelled.");load()}catch(e){setNotice(e.message)}}
 async function recordPayment(e){e.preventDefault();try{await api("/payments",{method:"POST",body:JSON.stringify({bookingId:Number(pay.bookingId),amount:Number(pay.amount),paymentDate:new Date().toISOString().slice(0,10),paymentMethod:pay.paymentMethod,status:"PAID"})});setNotice("Payment recorded as PAID.");setPay({bookingId:"",amount:"",paymentMethod:"Bank Transfer"});load()}catch(e){setNotice(e.message)}}
 async function completeSale(b){const payment=payments.find(p=>p.bookingId===b.bookingId&&p.status==="PAID");if(!payment){setNotice("Record a paid payment before completing the sale.");return}try{await api("/sales",{method:"POST",body:JSON.stringify({houseId:b.houseId,customerId:b.customerId,salePrice:Number(payment.amount),saleDate:new Date().toISOString().slice(0,10)})});setNotice("Sale completed. House marked SOLD.");load()}catch(e){setNotice(e.message)}}
 function printReport(){const w=window.open("","_blank");if(!w)return;w.document.write(`<html><head><title>Seller Sales Report</title><style>body{font-family:Arial;padding:30px}table{width:100%;border-collapse:collapse}td,th{border:1px solid #ccc;padding:8px;text-align:left}</style></head><body><h1>MAHA E-HOUSING — Seller Sales Report</h1><p>Seller: ${auth?.name||""}</p><table><tr><th>Sale</th><th>House</th><th>Customer</th><th>Date</th><th>Amount</th><th>Status</th></tr>${sales.map(s=>`<tr><td>#${s.saleId}</td><td>${s.houseTitle||s.houseId}</td><td>${s.customerName||s.customerId}</td><td>${s.saleDate||""}</td><td>${money(s.salePrice)}</td><td>${s.status||"SOLD"}</td></tr>`).join("")}</table><p><b>Total sales: ${sales.length} | Total value: ${money(sales.reduce((a,s)=>a+Number(s.salePrice||0),0))}</b></p><script>window.print()</script></body></html>`);w.document.close()}
 return <main className="dashboardShell"><DashboardSidebar role="SELLER" image={auth?.image} name={auth?.name} tab={tab} setTab={setTab} go={go} logout={logout} actionLabel={tab==="houses"?"Add house":"Print sales report"} onAction={tab==="houses"?openAdd:printReport} items={[{id:"overview",label:"Overview",icon:"◈"},{id:"houses",label:"My houses",icon:"⌂"},{id:"bookings",label:"Bookings received",icon:"◷"},{id:"payments",label:"Payments",icon:"◇"},{id:"sales",label:"Completed sales",icon:"✓"}]}/>
 <section className="dashboardMain pagePad"><div className="dashTop"><div><span className="eyebrow">SELLER PORTAL</span><h1>{tab==="houses"?"My house collection.":tab==="bookings"?"Bookings received.":tab==="payments"?"Payments received.":tab==="sales"?"Completed sales.":`Welcome, ${auth?.name||"seller"}.`}</h1><p>{tab==="overview"?"Manage your property business from one section at a time.":"Only the selected seller workspace is shown."}</p></div>{tab==="houses"&&<button className="primaryBtn" onClick={openAdd}>+ Add house</button>}{tab==="sales"&&<button className="primaryBtn" onClick={printReport}>Print report →</button>}</div>
 <section className="dashContent">
 {tab==="overview"&&<div className="metricRow"><div><small>My houses</small><b>{houses.length}</b></div><div><small>Bookings</small><b>{bookings.length}</b></div><div><small>Payments</small><b>{payments.length}</b></div><div><small>Sales</small><b>{sales.length}</b></div></div>}
 {tab==="houses"&&<div className="dashCard"><div className="sectionHead"><div><span className="eyebrow">MY COLLECTION</span><h2>Houses</h2></div><button className="primaryBtn" onClick={openAdd}>+ Add house</button></div>{houses.length?houses.map((h,i)=><div className="sellerHouse" key={h.houseId}><img src={h.image||images[i%images.length]}/><div><b>{h.title}</b><small>{h.location} · {money(h.price)} · {h.bedrooms} beds · {h.bathrooms} baths</small></div><button className="smallBtn" title="Edit house" aria-label={`Edit ${h.title||"house"}`} onClick={()=>openEdit(h)}>Edit</button><button className="smallBtn dangerBtn" title="Delete house" aria-label={`Delete ${h.title||"house"}`} onClick={()=>del(h.houseId)}>Delete</button></div>):<p className="muted">No houses yet. Use Add house to publish your first property.</p>}</div>}
 {tab==="bookings"&&<div className="dashCard"><h3>Booking status</h3>{bookings.length?bookings.map(b=>{const st=statusOf(b.status);return <div className="listRow" key={b.bookingId}><span>#{b.bookingId}</span><b>{b.houseTitle||`House #${b.houseId}`}</b><span>{b.customerName||`Customer #${b.customerId}`} · {b.bookingDate}</span><em className={`status ${st.toLowerCase()}`}>{st==="CONFIRMED"?"APPROVED":st}</em>{st==="PENDING"&&<><button className="smallBtn" onClick={()=>confirmBooking(b.bookingId)}>Approve</button><button className="smallBtn dangerBtn" onClick={()=>cancelBooking(b.bookingId)}>Reject</button></>}{st==="CONFIRMED"&&payments.some(p=>p.bookingId===b.bookingId&&statusOf(p.status)==="PAID")&&<button className="smallBtn" onClick={()=>completeSale(b)}>Complete sale</button>}</div>;}):<p className="muted">No bookings yet. When a customer requests a viewing, it will appear here.</p>}</div>}
 {tab==="payments"&&<><div className="dashCard"><h3>Record payment received</h3><p className="muted">Payment is active only for bookings that you have approved.</p>{!bookings.some(b=>statusOf(b.status)==="CONFIRMED"&&!payments.some(p=>p.bookingId===b.bookingId&&statusOf(p.status)==="PAID"))&&<div className="paymentLocked">No approved booking is ready for payment. Open <b>Bookings received</b>, approve a PENDING booking, then return here.</div>}<form className="formCard inlineForm" onSubmit={recordPayment}><select required value={pay.bookingId} onChange={e=>setPay({...pay,bookingId:e.target.value})}><option value="">Select approved booking</option>{bookings.filter(b=>statusOf(b.status)==="CONFIRMED"&&!payments.some(p=>p.bookingId===b.bookingId&&statusOf(p.status)==="PAID")).map(b=><option key={b.bookingId} value={b.bookingId}>#{b.bookingId} — {b.customerName||`Customer #${b.customerId}`} — {b.houseTitle||`House #${b.houseId}`}</option>)}</select><input type="number" min="1" required placeholder="Amount" value={pay.amount} onChange={e=>setPay({...pay,amount:e.target.value})}/><select value={pay.paymentMethod} onChange={e=>setPay({...pay,paymentMethod:e.target.value})}><option>Bank Transfer</option><option>Mobile Money</option><option>Cash</option></select><button className="primaryBtn" disabled={!bookings.some(b=>statusOf(b.status)==="CONFIRMED"&&!payments.some(p=>p.bookingId===b.bookingId&&statusOf(p.status)==="PAID"))}>Mark PAID →</button></form></div><DataList title="Payment status" data={payments}/></>}
 {tab==="sales"&&<DataList title="Completed purchases" data={sales}/>}</section></section>
 {showAdd&&<Modal title={editing?"Edit house":"Add house"} onClose={()=>{setShowAdd(false);setEditing(null)}}><form className="formCard" onSubmit={save}>{["title","location","description","price","bedrooms","bathrooms"].map(k=><input key={k} required placeholder={k[0].toUpperCase()+k.slice(1)} value={form[k]} onChange={e=>setForm({...form,[k]:e.target.value})}/>)}<label className="fileField">House picture<input type="file" accept="image/*" onChange={e=>readImage(e.target.files?.[0],image=>setForm({...form,image}))}/></label><button className="primaryBtn wide">{editing?"Update house":"Publish house"} →</button></form></Modal>}
 </main>
}

function Modal({title,onClose,children}){return <div className="modalBackdrop" onMouseDown={e=>{if(e.target===e.currentTarget)onClose()}}><div className="modalBox"><div className="modalHead"><div><span className="eyebrow">MAHA E-HOUSING</span><h2>{title}</h2></div><button type="button" className="modalClose" onClick={onClose}>×</button></div>{children}</div></div>}

function AdminPeopleCollection({title,type,data,onEdit,onDelete,onSelect,onAdd}){
 const [query,setQuery]=useState("");
 const isSeller=type==="sellers";
 const filtered=data.filter(row=>`${row.name||""} ${row.email||""} ${row.nida||""} ${row[isSeller?"sellerId":"customerId"]||""}`.toLowerCase().includes(query.toLowerCase()));
 return <div className="peopleCollection"><div className="collectionHead"><div><span className="eyebrow">DIRECTORY</span><h2>{title}</h2><p className="muted">{data.length} registered {type}.</p></div><div className="collectionActions">{onAdd&&<button className="primaryBtn" onClick={onAdd}>+ Add seller</button>}<input className="collectionSearch" value={query} onChange={e=>setQuery(e.target.value)} placeholder={`Search ${type}...`}/></div></div>{filtered.length?<div className="peopleGrid">{filtered.map((row,i)=>{const id=isSeller?row.sellerId:row.customerId;return <article className={`personCard selectable ${onSelect?"clickable":""}`} key={id||i} onClick={()=>onSelect&&onSelect(row)}>{row.image?<img className="personPhoto" src={row.image} alt=""/>:<div className="personPhoto placeholder">{initials(row.name||"User")}</div>}<div className="personInfo"><div className="personTop"><span className="personRole">{isSeller?"SELLER":"CUSTOMER"}</span><b>#{id??"—"}</b></div><h3>{row.name||"Unnamed user"}</h3><div className="personDetails"><span><strong>Email</strong>{row.email||"—"}</span><span><strong>NIDA</strong>{row.nida||"—"}</span></div>{onEdit&&<div className="personActions"><button className="smallBtn" title={`Edit ${isSeller?"seller":"customer"}`} aria-label={`Edit ${row.name||type.slice(0,-1)}`} onClick={e=>{e.stopPropagation();onEdit(row)}}>Edit</button><button className="smallBtn dangerBtn" title={`Delete ${isSeller?"seller":"customer"}`} aria-label={`Delete ${row.name||type.slice(0,-1)}`} onClick={e=>{e.stopPropagation();onDelete(row)}}>Delete</button></div>}</div></article>})}</div>:<div className="empty"><h3>No {type} found</h3><p>Try another search.</p></div>}</div>
}

function AdminDesk({setNotice,go,logout}){
 const [tab,setTab]=useState("overview"),[records,setRecords]=useState({sellers:[],customers:[],houses:[],bookings:[],payments:[],sales:[],documents:[]});
 const emptySeller={name:"",email:"",phone:"",address:"",nida:"",password:"",image:""};
 const emptyCustomer={name:"",email:"",phone:"",address:"",nida:"",password:"",image:""};
 const [sellerForm,setSellerForm]=useState(emptySeller),[busy,setBusy]=useState(false),[showSeller,setShowSeller]=useState(false),[editingSeller,setEditingSeller]=useState(null),[selectedSeller,setSelectedSeller]=useState(null);
 const [customerForm,setCustomerForm]=useState(emptyCustomer),[showCustomer,setShowCustomer]=useState(false),[editingCustomer,setEditingCustomer]=useState(null);
 async function loadAll(){try{const names=Object.keys(records);const values=await Promise.all(names.map(n=>api(`/management/${n}`)));const next={};names.forEach((n,i)=>next[n]=Array.isArray(values[i])?values[i]:[]);setRecords(next)}catch(e){setNotice(e.message)}}
 useEffect(()=>{loadAll()},[]);
 function openAddSeller(){setEditingSeller(null);setSellerForm(emptySeller);setShowSeller(true)}
 function openEditSeller(row){setEditingSeller(row.sellerId);setSellerForm({...row,password:""});setShowSeller(true)}
 async function saveSeller(e){e.preventDefault();setBusy(true);try{const body={...sellerForm};if(editingSeller){if(!body.password)body.password="ChangeMe123!";await api(`/management/sellers/${editingSeller}`,{method:"PUT",body:JSON.stringify(body)});setNotice("Seller updated successfully.")}else{await api("/management/sellers",{method:"POST",body:JSON.stringify(body)});setNotice("Seller account created successfully.")}setShowSeller(false);setEditingSeller(null);setSellerForm(emptySeller);await loadAll()}catch(e){setNotice(e.message)}finally{setBusy(false)}}
 async function deleteSeller(row){if(!confirm(`Delete seller ${row.name}? This is allowed only when the seller has no houses.`))return;try{await api(`/management/sellers/${row.sellerId}`,{method:"DELETE"});setNotice("Seller deleted.");setSelectedSeller(null);loadAll()}catch(e){setNotice(e.message)}}
 function openEditCustomer(row){setEditingCustomer(row.customerId);setCustomerForm({...row,password:""});setShowCustomer(true)}
 async function saveCustomer(e){e.preventDefault();setBusy(true);try{const body={...customerForm};await api(`/management/customers/${editingCustomer}`,{method:"PUT",body:JSON.stringify(body)});setNotice("Customer updated successfully.");setShowCustomer(false);setEditingCustomer(null);setCustomerForm(emptyCustomer);await loadAll()}catch(e){setNotice(e.message)}finally{setBusy(false)}}
 async function deleteCustomer(row){if(!confirm(`Delete customer ${row.name}? This is allowed only when the customer has no bookings or sales.`))return;try{await api(`/management/customers/${row.customerId}`,{method:"DELETE"});setNotice("Customer deleted.");await loadAll()}catch(e){setNotice(e.message)}}
 const totalSales=records.sales.reduce((a,s)=>a+Number(s.salePrice||0),0),paid=records.payments.reduce((a,p)=>a+Number(p.amount||0),0); const sellerMap={};records.houses.forEach(h=>sellerMap[h.houseId]=h.sellerName||`Seller ${h.sellerId||""}`);const grouped={};records.sales.forEach(s=>{const seller=sellerMap[s.houseId]||"Unknown seller";grouped[seller]=(grouped[seller]||0)+Number(s.salePrice||0)});
 function printSellerSales(){const w=window.open("","_blank");if(!w)return;w.document.write(`<html><head><title>Seller Sales Report</title><style>body{font-family:Arial;padding:30px}table{width:100%;border-collapse:collapse}td,th{border:1px solid #ccc;padding:8px}</style></head><body><h1>MAHA E-HOUSING — Seller Sales</h1><table><tr><th>Seller</th><th>Sales value</th></tr>${Object.entries(grouped).map(([n,v])=>`<tr><td>${n}</td><td>${money(v)}</td></tr>`).join("")}</table><h3>Total sales: ${money(totalSales)}</h3><script>window.print()</script></body></html>`);w.document.close()}
 const title={overview:"Control center",sellers:"Seller collection",customers:"Customer collection",houses:"All houses",bookings:"Bookings",payments:"Payments",sales:"Sales by seller"}[tab];
 return <main className="dashboardShell"><DashboardSidebar role="ADMIN" name="Administrator" tab={tab} setTab={setTab} go={go} logout={logout} actionLabel={tab==="sellers"?"Add seller":tab==="sales"?"Print seller report":null} onAction={tab==="sellers"?openAddSeller:printSellerSales} items={[{id:"overview",label:"Overview",icon:"◈"},{id:"sellers",label:"Sellers",icon:"♙"},{id:"customers",label:"Customers",icon:"♧"},{id:"houses",label:"All houses",icon:"⌂"},{id:"bookings",label:"Bookings",icon:"◷"},{id:"payments",label:"Payments",icon:"◇"},{id:"sales",label:"Sales by seller",icon:"✓"}]}/><section className="dashboardMain pagePad"><div className="dashTop"><div><span className="eyebrow">ADMIN PORTAL</span><h1>MAHA E-HOUSING<br/>{title}.</h1><p>{tab==="sellers"?"Manage seller accounts and open an individual seller record.":tab==="customers"?"Manage customer profiles, update their details, or remove eligible records.":"Use the sidebar to open one management area at a time."}</p></div>{tab==="sellers"&&<button className="primaryBtn" onClick={openAddSeller}>+ Add seller</button>}{tab==="sales"&&<button className="primaryBtn" onClick={printSellerSales}>Print report →</button>}</div>
 {tab==="overview"&&<div className="dashContent"><div className="metricRow adminMetrics"><div><small>Total customers</small><b>{records.customers.length}</b></div><div><small>Total sellers</small><b>{records.sellers.length}</b></div><div><small>Total houses</small><b>{records.houses.length}</b></div><div><small>Total sales</small><b>{records.sales.length}</b></div><div><small>Payments received</small><b>{money(paid)}</b></div><div><small>Sales value</small><b>{money(totalSales)}</b></div></div><div className="dashCard"><h3>Management center</h3><p className="muted">Select one collection from the sidebar. Seller and customer records are kept separate.</p></div></div>}
 {tab==="sellers"&&<><AdminPeopleCollection title="All sellers" type="sellers" data={records.sellers} onAdd={openAddSeller} onEdit={openEditSeller} onDelete={deleteSeller} onSelect={setSelectedSeller}/>{selectedSeller&&<div className="dashCard selectedPerson"><div className="sectionHead"><div><span className="eyebrow">SELLER DETAILS</span><h2>{selectedSeller.name}</h2></div><div><button className="primaryBtn" onClick={openAddSeller}>+ Add seller</button><button type="button" className="outlineBtn" onClick={()=>openEditSeller(selectedSeller)}>Edit seller</button></div></div><div className="sellerDetailGrid"><img className="personPhoto" src={selectedSeller.image||""} alt=""/><div><b>Seller ID: #{selectedSeller.sellerId}</b><p>Email: {selectedSeller.email}</p><p>NIDA: {selectedSeller.nida}</p><p>Phone: {selectedSeller.phone}</p><p>Address: {selectedSeller.address}</p></div></div></div>}</>}
 {tab==="customers"&&<AdminPeopleCollection title="All customers" type="customers" data={records.customers} onEdit={openEditCustomer} onDelete={deleteCustomer}/>} 
 {tab==="houses"&&<div className="tableCard"><AdminTable data={records.houses} empty="No houses yet"/></div>}
 {tab==="bookings"&&<div className="tableCard"><AdminTable data={records.bookings} empty="No bookings yet"/></div>}
 {tab==="payments"&&<div className="tableCard"><AdminTable data={records.payments} empty="No payments yet"/></div>}
 {tab==="sales"&&<div className="dashCard"><div className="sectionHead"><div><span className="eyebrow">REPORT</span><h2>Sales by seller</h2></div><button className="primaryBtn" onClick={printSellerSales}>Print report →</button></div>{Object.keys(grouped).length?Object.entries(grouped).map(([name,value])=><div className="listRow" key={name}><b>{name}</b><span>{money(value)}</span><em>SALES VALUE</em></div>):<p className="muted">No completed sales yet.</p>}</div>}
 </section>{showSeller&&<Modal title={editingSeller?"Edit seller":"Add seller"} onClose={()=>{setShowSeller(false);setEditingSeller(null)}}><form className="formCard" onSubmit={saveSeller}><input placeholder="Seller full name" required value={sellerForm.name} onChange={e=>setSellerForm({...sellerForm,name:e.target.value})}/><input type="email" placeholder="Seller email" required value={sellerForm.email} onChange={e=>setSellerForm({...sellerForm,email:e.target.value})}/><div className="two"><input placeholder="Phone" required value={sellerForm.phone} onChange={e=>setSellerForm({...sellerForm,phone:e.target.value})}/><input placeholder="NIDA" required value={sellerForm.nida} onChange={e=>setSellerForm({...sellerForm,nida:e.target.value})}/></div><input placeholder="Address" required value={sellerForm.address} onChange={e=>setSellerForm({...sellerForm,address:e.target.value})}/><label className="fileField">Seller passport picture<input type="file" accept="image/*" onChange={e=>readImage(e.target.files?.[0],image=>setSellerForm({...sellerForm,image}))}/></label><input type="password" minLength="6" placeholder={editingSeller?"New password (optional)":"Seller password"} required={!editingSeller} value={sellerForm.password} onChange={e=>setSellerForm({...sellerForm,password:e.target.value})}/><button className="primaryBtn wide" disabled={busy}>{busy?"Saving…":editingSeller?"Update seller →":"Create seller →"}</button></form></Modal>}{showCustomer&&<Modal title="Edit customer" onClose={()=>{setShowCustomer(false);setEditingCustomer(null)}}><form className="formCard" onSubmit={saveCustomer}><input placeholder="Customer full name" required value={customerForm.name} onChange={e=>setCustomerForm({...customerForm,name:e.target.value})}/><input type="email" placeholder="Customer email" required value={customerForm.email} onChange={e=>setCustomerForm({...customerForm,email:e.target.value})}/><div className="two"><input placeholder="Phone" required value={customerForm.phone} onChange={e=>setCustomerForm({...customerForm,phone:e.target.value})}/><input placeholder="NIDA" required value={customerForm.nida} onChange={e=>setCustomerForm({...customerForm,nida:e.target.value})}/></div><input placeholder="Address" required value={customerForm.address} onChange={e=>setCustomerForm({...customerForm,address:e.target.value})}/><label className="fileField">Customer passport picture<input type="file" accept="image/*" onChange={e=>readImage(e.target.files?.[0],image=>setCustomerForm({...customerForm,image}))}/></label><input type="password" minLength="6" placeholder="New password (optional)" value={customerForm.password} onChange={e=>setCustomerForm({...customerForm,password:e.target.value})}/><button className="primaryBtn wide" disabled={busy}>{busy?"Saving…":"Update customer →"}</button></form></Modal>}</main>
}

function AdminTable({data,empty}){return data.length?<table><thead><tr>{Object.keys(data[0]).filter(k=>k!=="image").slice(0,7).map(k=><th key={k}>{k}</th>)}</tr></thead><tbody>{data.map((row,i)=><tr key={i}>{Object.entries(row).filter(([k])=>k!=="image").slice(0,7).map(([k,v],j)=><td key={j}>{typeof v==="object"?JSON.stringify(v):String(v??"")}</td>)}</tr>)}</tbody></table>:<div className="empty"><h3>{empty}</h3></div>}

createRoot(document.getElementById("root")).render(<App/>);
