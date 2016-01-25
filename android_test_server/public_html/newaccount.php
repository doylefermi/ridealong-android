<?php
include("config.php");
session_start();
if($_SERVER["REQUEST_METHOD"] == "POST")
{
// username and password sent from Form
$myname=mysqli_real_escape_string($db,$_POST['name']);
$myusername=mysqli_real_escape_string($db,$_POST['username']);
$mypassword=mysqli_real_escape_string($db,$_POST['password']);

$sql="INSERT into admin (name,username,passcode) values('$myname','$myusername','$mypassword')";

$insert_row = $db->query($sql);

if($insert_row){
    session_register("myusername");
	$_SESSION['login_user']=$myusername;
	header("location: welcome.php",true,202);
	
}else{
    die('Error : ('. $db->errno .') '. $mysqli->error);
}
/*****************************same as locationpost.php*************************************/
$sql="SELECT * FROM admin WHERE username='$myusername';";
$result=mysqli_query($db,$sql);

$mylatitude=0;
$mylongitude=0;
$myrstatus=0;
$myconnecter=0;
if ($result->num_rows > 0) {
	$row = $result->fetch_assoc();
	$id=$row["id"];
	$sql1="INSERT into location (id,latitude,longitude,rstatus,connecter)values('$id','$mylatitude','$mylongitude','$myrstatus','$myconnecter');";
	$result1=mysqli_query($db,$sql1);
	$data = array('username'=>$myusername, 'latitude'=>$mylatitude, 'longitude'=>$mylongitude,'rstatus'=>$myrstatus,'connecter'=>$myconnecter);
	header('Content-Type: application/json');
	echo json_encode($data);
}
else { echo "0 results";}
/*****************************same as locationpost.php*************************************/
}
?>
<form action="" method="post">
<label>Name :</label>
<input type="text" name="name"/><br />
<label>UserName :</label>
<input type="text" name="username"/><br />
<label>Password :</label>
<input type="password" name="password"/><br/>
<input type="submit" value=" Submit "/><br />
</form>
