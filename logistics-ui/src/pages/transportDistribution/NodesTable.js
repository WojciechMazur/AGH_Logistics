import React from 'react';
import {withStyles} from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import Tab from "@material-ui/core/Tab/Tab";
import Tabs from "@material-ui/core/Tabs/Tabs";
import Typography from '@material-ui/core/Typography';
import ConnectionsManager from "../../components/transportDistribution/ConnectionsManager";
import SupplierAddForm from "../../components/transportDistribution/supplier/SupplierAddForm"
import {SimpleConnection, Recipient, Supplier} from "../../types";
import Dashboard from "./Dashboard";
import RecipientAddForm from "../../components/transportDistribution/recipient/RecipientAddForm";
import {SupplierEditForm} from "../../components/transportDistribution/supplier/SupplierEditForm";
import EditFormModal from "../../components/EditFormModal";
import {RecipientEditForm} from "../../components/transportDistribution/recipient/RecipientEditForm";
import Button from "@material-ui/core/Button/Button";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import type {Connection} from "../../types";


const TabNames = {
    SUPPLIERS: 0,
    RECIPIENTS: 1,
    CONNECTIONS: 2
};

type Props = {
    connections: Array<SimpleConnection>,
    suppliers: Array<Supplier>,
    recipients: Array<Recipient>,
    dashboardRef: Dashboard
}

type State = {
    currentTab: number,
    standardIsResolved: boolean,
    mediatorIsResolved: boolean
}

export function TabContainer(props) {
    return (
        <Typography component="div" style={{ padding: 8 * 3 }}>
            {props.children}
        </Typography>
    );
}

class NodesTableTabs extends React.Component<Props, State> {
    constructor(props) {
        super(props);
        this.state = {
            currentTab: TabNames.SUPPLIERS,
            standardIsResolved: false,
            mediatorIsResolved: false
        };
    }

    handleChange = (event, value) => {
        this.setState({currentTab: value});
    };

    drawSuppliers = () => {
      return <Paper className="SuppliersPaper">
          <Table className="SuppliersTable">
              <TableHead>
                  <TableRow>
                      <TableCell>Supplier name</TableCell>
                      <TableCell numeric>Available</TableCell>
                      <TableCell numeric>Supply</TableCell>
                      <TableCell numeric>Priority</TableCell>
                      <TableCell numeric>Limit</TableCell>
                      <TableCell numeric>Purchase cost</TableCell>
                      <TableCell/>
                  </TableRow>
              </TableHead>
              <TableBody>
                  {this.props.suppliers.map(elem => {
                      return (
                          <TableRow key={elem.id}>
                              <TableCell component="th" scope="row">
                                  {elem.name}
                              </TableCell>
                              <TableCell numeric>{elem.available}</TableCell>
                              <TableCell numeric>{elem.supply}</TableCell>
                              <TableCell numeric>{elem.priority || "None"}</TableCell>
                              <TableCell numeric>{elem.limit || "None"}</TableCell>
                              <TableCell numeric>{elem.purchaseCost || "None"}</TableCell>
                              <TableCell>
                                  <div style={{display: "inline-flex"}}>
                                  <EditFormModal>
                                      <SupplierEditForm supplier={elem} dashboardRef={this.props.dashboardRef}/>
                                  </EditFormModal>
                                  <Button onClick={() => this.props.dashboardRef.handleRemoveSupplier(elem)}>
                                      <FontAwesomeIcon icon="minus"/>
                                  </Button>
                                  </div>
                              </TableCell>
                          </TableRow>
                      );
                  })}
              </TableBody>
          </Table>
          <SupplierAddForm dashboardRef={this.props.dashboardRef}/>
      </Paper>
    };

    drawRecipients = () => {
        return <Paper className="RecipientsPaper">
            <Table className="RecipientsTable">
                <TableHead>
                    <TableRow>
                        <TableCell>Recipient name</TableCell>
                        <TableCell numeric>Available</TableCell>
                        <TableCell numeric>Demand</TableCell>
                        <TableCell numeric>Priority</TableCell>
                        <TableCell numeric>Limit</TableCell>
                        <TableCell numeric>Selling price</TableCell>
                        <TableCell/>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {this.props.recipients.map(elem => {
                        return (
                            <TableRow key={elem.id}>
                                <TableCell component="th" scope="row">
                                    {elem.name}
                                </TableCell>
                                <TableCell numeric>{elem.available}</TableCell>
                                <TableCell numeric>{elem.demand}</TableCell>
                                <TableCell numeric>{elem.priority || "None"}</TableCell>
                                <TableCell numeric>{elem.limit || "None"}</TableCell>
                                <TableCell numeric>{elem.saleProfit || "None"}</TableCell>
                                <TableCell>
                                    <div style={{display: "inline-flex"}}>
                                        <EditFormModal>
                                            <RecipientEditForm recipient={elem} dashboardRef={this.props.dashboardRef}/>
                                        </EditFormModal>
                                        <Button onClick={() => this.props.dashboardRef.handleRemoveRecipient(elem)}>
                                            <FontAwesomeIcon icon="minus"/>
                                        </Button>
                                    </div>
                                </TableCell>
                            </TableRow>
                        );
                    })}
                </TableBody>
            </Table>
            <RecipientAddForm dashboardRef={this.props.dashboardRef}/>
        </Paper>
    };

    calcMediatorProfit() {
        return this.props.connections.map(c =>
            c.attributes.units * (c.attributes.unitSaleProfit - c.attributes.unitPurchaseCost - c.attributes.unitTransportCost)
        ).reduce((acc, x) => acc + x)
    }

    calcTargetValue(){
        return this.props.connections.map(c => c.attributes.units*c.attributes.unitTransportCost).reduce((acc, x) => acc + x)

    }

    handleResolveMediatorButton() {
        fetch('http://localhost:8080/transport/mediator', {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(this.props.connections)
        }).then((value, err) => {
            err && console.error(err);
            if (value) {
                return value.json()
            }
            return [];
        }).then((connections: Array<Connection>) => {
                this.props.dashboardRef.handleUpdateConnections(connections);
                this.setState({
                    mediatorIsResolved: true
                });
            }
        )
    }

    handleResolveButton = () => {
        fetch('http://localhost:8080/transport/standard', {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(this.props.connections)
        }).then((value, err) => {
            err && console.error(err);
            if (value) {
                return value.json()
            }
            return [];
        }).then((connections: Array<Connection>) => {
                this.props.dashboardRef.handleUpdateConnections(connections);
                this.setState({
                    standardIsResolved: true
                });
            }
        )
    };

    isReadyForStandardProblem = () => this.props.connections
        .every(c => c.attributes.unitTransportCost && c.attributes.unitTransportCost !== 0);

    isReadyForMediatorProblem = () => this.isReadyForStandardProblem() &&
        this.props.suppliers.every(s => s.purchaseCost && s.purchaseCost !== 0) &&
        this.props.recipients.every(r => r.saleProfit && r.saleProfit !== 0);

    render() {
        const {classes} = this.props;
        const currentTab = this.state.currentTab;
        return (
            <Paper className={classes.root}>
                <Tabs
                    value={this.state.currentTab}
                    onChange={this.handleChange}
                    indicatorColor={"primary"}
                    textColor={"primary"}
                    centered
                    >
                    <Tab label={"Suppliers"}/>
                    <Tab label={"Recipients"}/>
                    <Tab label="Connections"/>
                </Tabs>
                {currentTab === 0 && <TabContainer>{this.drawSuppliers()}</TabContainer>}
                {currentTab === 1 && <TabContainer>{this.drawRecipients()}</TabContainer>}
                {currentTab === 2 && <TabContainer>
                    <ConnectionsManager {...this.props}/>
                </TabContainer>}

                <Paper square={true}>
                    <Button fullWidth={true}
                            variant="contained"
                            color="primary"
                            disabled={!this.isReadyForStandardProblem()}
                            onClick={()=> this.handleResolveButton()}>Resolve standard problem</Button>
                    <br/>
                    {this.state.standardIsResolved &&
                        <Typography align="center" variant="h6">
                        Target function of standard problem: <strong>{this.calcTargetValue()}</strong>
                        </Typography>
                    }
                </Paper>

                <Paper square={true}>
                    <Button fullWidth={true}
                            variant="contained"
                            color="primary"
                            disabled={!this.isReadyForMediatorProblem()}
                            onClick={()=> this.handleResolveMediatorButton()}>Resolve mediator problem</Button>
                    <br/>
                    {this.state.mediatorIsResolved &&
                    <Typography align="center" variant="h6">
                        Target function of mediator problem: <strong>{this.calcMediatorProfit().toString()}</strong>
                    </Typography>
                    }
                </Paper>
            </Paper>
        );
    }


}

const styles = {
    root: {
        width: '100%',
        overflowX: 'auto',
    },
    table: {
        minWidth: 700,
    }
};

export default withStyles(styles)(NodesTableTabs);
